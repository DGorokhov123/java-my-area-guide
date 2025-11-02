package ru.practicum.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.dto.State;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequestDto;
import ru.practicum.request.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    // ЗАЯВКИ ТЕКУЩЕГО ПОЛЬЗОВАТЕЛЯ

    // Добавление запроса от текущего пользователя на участие в событии
    @Transactional(readOnly = false)
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        // нельзя добавить повторный запрос (Ожидается код ошибки 409)
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("User tries to make duplicate request", "Forbidden action");
        }

        // инициатор события не может добавить запрос на участие в своём событии (Ожидается код ошибки 409)
        if (Objects.equals(requester.getId(), event.getInitiator().getId())) {
            throw new ConflictException("User tries to request for his own event", "Forbidden action");
        }

        // нельзя участвовать в неопубликованном событии (Ожидается код ошибки 409)
        if (event.getState() != State.PUBLISHED) {
            throw new ConflictException("User tries to request for non-published event", "Forbidden action");
        }

        // если у события достигнут лимит запросов на участие - необходимо вернуть ошибку (Ожидается код ошибки 409)
        long confirmedRequestCount = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmedRequestCount >= event.getParticipantLimit()) {
            throw new ConflictException("Participants limit is already reached", "Forbidden action");
        }

        // если для события отключена пре-модерация запросов на участие, то запрос должен автоматически перейти в состояние подтвержденного
        ParticipationRequestStatus newRequestStatus = ParticipationRequestStatus.PENDING;
        if (!event.getRequestModeration()) newRequestStatus = ParticipationRequestStatus.CONFIRMED;
        if (Objects.equals(event.getParticipantLimit(), 0L)) newRequestStatus = ParticipationRequestStatus.CONFIRMED;

        Request newRequest = Request.builder()
                .requester(requester)
                .event(event)
                .status(newRequestStatus)
                .created(LocalDateTime.now())
                .build();
        requestRepository.save(newRequest);
        return RequestMapper.toDto(newRequest);
    }

    // Отмена своего запроса на участие в событии
    @Transactional(readOnly = false)
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Request existingRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));

        existingRequest.setStatus(ParticipationRequestStatus.CANCELED);
        requestRepository.save(existingRequest);
        return RequestMapper.toDto(existingRequest);
    }

    // Получение информации о заявках текущего пользователя на участие в чужих событиях
    public Collection<ParticipationRequestDto> findRequesterRequests(Long userId) {
        return requestRepository.findByRequesterId(userId).stream()
                .filter(Objects::nonNull)
                .map(RequestMapper::toDto)
                .toList();
    }

    // ЗАЯВКИ НА КОНКРЕТНОЕ СОБЫТИЕ

    // Получение информации о запросах на участие в событии текущего пользователя
    public Collection<ParticipationRequestDto> findEventRequests(Long userId, Long eventId) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        // проверка что юзер - инициатор события
        if (!Objects.equals(initiator.getId(), event.getInitiator().getId())) {
            throw new ConflictException("User " + userId + " is not an initiator of event " + eventId, "Forbidden action");
        }

        return requestRepository.findByEventId(eventId).stream()
                .filter(Objects::nonNull)
                .map(RequestMapper::toDto)
                .toList();
    }

    // Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя
    @Transactional(readOnly = false)
    public EventRequestStatusUpdateResultDto moderateRequest(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequestDto updateRequestDto
    ) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        // проверка что юзер - инициатор события
        if (!Objects.equals(initiator.getId(), event.getInitiator().getId())) {
            throw new ConflictException("User " + userId + " is not an initiator of event " + eventId, "Forbidden action");
        }

        // если для события лимит заявок равен 0 или отключена пре-модерация заявок, то подтверждение заявок не требуется
        if (event.getParticipantLimit() < 1 || !event.getRequestModeration()) {
            return new EventRequestStatusUpdateResultDto();
        }

        // статус можно изменить только у заявок, находящихся в состоянии ожидания (Ожидается код ошибки 409)
        List<Request> requests = requestRepository.findAllById(updateRequestDto.getRequestIds());
        for (Request request : requests) {
            if (request.getStatus() != ParticipationRequestStatus.PENDING) {
                throw new ConflictException("Request " + request.getId() + " must have status PENDING", "Incorrectly made request");
            }
        }

        List<Long> requestsToConfirm = new ArrayList<>();
        List<Long> requestsToReject = new ArrayList<>();

        if (updateRequestDto.getStatus() == ParticipationRequestStatus.CONFIRMED) {

            long confirmedRequestCount = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);

            if (confirmedRequestCount >= event.getParticipantLimit()) {
                // нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие (Ожидается код ошибки 409)
                throw new ConflictException("The participant limit has been reached for event " + eventId, "Forbidden action");
            } else if (updateRequestDto.getRequestIds().size() < event.getParticipantLimit() - confirmedRequestCount) {
                requestsToConfirm = updateRequestDto.getRequestIds();
                requestRepository.updateStatusByIds(requestsToConfirm, ParticipationRequestStatus.CONFIRMED);
            } else {
                long freeSeats = event.getParticipantLimit() - confirmedRequestCount;
                requestsToConfirm = updateRequestDto.getRequestIds().stream()
                        .limit(freeSeats)
                        .toList();
                requestsToReject = updateRequestDto.getRequestIds().stream()
                        .skip(freeSeats)
                        .toList();
                requestRepository.updateStatusByIds(requestsToConfirm, ParticipationRequestStatus.CONFIRMED);
                // если при подтверждении данной заявки, лимит заявок для события исчерпан, то все неподтверждённые заявки необходимо отклонить
                requestRepository.setStatusToRejectForAllPending(eventId);
            }

        } else if (updateRequestDto.getStatus() == ParticipationRequestStatus.REJECTED) {
            requestsToReject = updateRequestDto.getRequestIds();
            requestRepository.updateStatusByIds(requestsToReject, ParticipationRequestStatus.REJECTED);
        } else {
            throw new ConflictException("Only CONFIRMED and REJECTED statuses are allowed", "Forbidden action");
        }

        EventRequestStatusUpdateResultDto resultDto = new EventRequestStatusUpdateResultDto();
        List<ParticipationRequestDto> confirmedRequests = requestRepository.findAllById(requestsToConfirm).stream()
                .map(RequestMapper::toDto)
                .toList();
        resultDto.setConfirmedRequests(confirmedRequests);
        List<ParticipationRequestDto> rejectedRequests = requestRepository.findAllById(requestsToReject).stream()
                .map(RequestMapper::toDto)
                .toList();
        resultDto.setRejectedRequests(rejectedRequests);
        return resultDto;
    }

}
