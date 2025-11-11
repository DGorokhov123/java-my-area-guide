package ru.practicum.request.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.event.EventInteractionDto;
import ru.practicum.dto.event.State;
import ru.practicum.dto.request.EventRequestStatusUpdateRequestDto;
import ru.practicum.dto.request.EventRequestStatusUpdateResultDto;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.ParticipationRequestStatus;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dal.Request;
import ru.practicum.request.dal.RequestRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;

    private final UserClient userClient;
    private final EventClient eventClient;

    // ЗАЯВКИ ТЕКУЩЕГО ПОЛЬЗОВАТЕЛЯ

    // Добавление запроса от текущего пользователя на участие в событии
    @Transactional
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        try {
            userClient.checkUser(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Not confirmed the existence of User " + userId);
        }

        EventInteractionDto eventDto;
        try {
            eventDto = eventClient.getEventInteractionDto(eventId);
        } catch (FeignException e) {
            throw new NotFoundException("Not confirmed the existence of Event " + eventId);
        }

        // нельзя добавить повторный запрос (Ожидается код ошибки 409)
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("User tries to make duplicate request", "Forbidden action");
        }

        // инициатор события не может добавить запрос на участие в своём событии (Ожидается код ошибки 409)
        if (Objects.equals(userId, eventDto.getInitiatorId())) {
            throw new ConflictException("User tries to request for his own event", "Forbidden action");
        }

        // нельзя участвовать в неопубликованном событии (Ожидается код ошибки 409)
        if (eventDto.getState() != State.PUBLISHED) {
            throw new ConflictException("User tries to request for non-published event", "Forbidden action");
        }

        // если у события достигнут лимит запросов на участие - необходимо вернуть ошибку (Ожидается код ошибки 409)
        long confirmedRequestCount = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
        if (eventDto.getParticipantLimit() > 0 && confirmedRequestCount >= eventDto.getParticipantLimit()) {
            throw new ConflictException("Participants limit is already reached", "Forbidden action");
        }

        // если для события отключена пре-модерация запросов на участие, то запрос должен автоматически перейти в состояние подтвержденного
        ParticipationRequestStatus newRequestStatus = ParticipationRequestStatus.PENDING;
        if (!eventDto.getRequestModeration()) newRequestStatus = ParticipationRequestStatus.CONFIRMED;
        if (Objects.equals(eventDto.getParticipantLimit(), 0L)) newRequestStatus = ParticipationRequestStatus.CONFIRMED;

        Request newRequest = Request.builder()
                .requesterId(userId)
                .eventId(eventId)
                .status(newRequestStatus)
                .created(LocalDateTime.now())
                .build();
        requestRepository.save(newRequest);
        return RequestMapper.toDto(newRequest);
    }

    // Отмена своего запроса на участие в событии
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        try {
            userClient.checkUser(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Not confirmed the existence of User " + userId);
        }
        Request existingRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));

        existingRequest.setStatus(ParticipationRequestStatus.CANCELED);
        requestRepository.save(existingRequest);
        return RequestMapper.toDto(existingRequest);
    }

    // Получение информации о заявках текущего пользователя на участие в чужих событиях
    @Transactional(readOnly = true)
    public Collection<ParticipationRequestDto> findRequesterRequests(Long userId) {
        return requestRepository.findByRequesterId(userId).stream()
                .filter(Objects::nonNull)
                .map(RequestMapper::toDto)
                .toList();
    }

    // ЗАЯВКИ НА КОНКРЕТНОЕ СОБЫТИЕ

    // Получение информации о запросах на участие в событии текущего пользователя
    @Transactional(readOnly = true)
    public Collection<ParticipationRequestDto> findEventRequests(Long userId, Long eventId) {
        try {
            userClient.checkUser(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Not confirmed the existence of User " + userId);
        }

        EventInteractionDto eventDto;
        try {
            eventDto = eventClient.getEventInteractionDto(eventId);
        } catch (FeignException e) {
            throw new NotFoundException("Not confirmed the existence of Event " + eventId);
        }

        // проверка что юзер - инициатор события
        if (!Objects.equals(userId, eventDto.getInitiatorId())) {
            throw new ConflictException("User " + userId + " is not an initiator of event " + eventId, "Forbidden action");
        }

        return requestRepository.findByEventId(eventId).stream()
                .filter(Objects::nonNull)
                .map(RequestMapper::toDto)
                .toList();
    }

    // Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя
    @Transactional
    public EventRequestStatusUpdateResultDto moderateRequest(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequestDto updateRequestDto
    ) {
        try {
            userClient.checkUser(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Not confirmed the existence of User " + userId);
        }

        EventInteractionDto eventDto;
        try {
            eventDto = eventClient.getEventInteractionDto(eventId);
        } catch (FeignException e) {
            throw new NotFoundException("Not confirmed the existence of Event " + eventId);
        }

        // проверка что юзер - инициатор события
        if (!Objects.equals(userId, eventDto.getInitiatorId())) {
            throw new ConflictException("User " + userId + " is not an initiator of event " + eventId, "Forbidden action");
        }

        // если для события лимит заявок равен 0 или отключена пре-модерация заявок, то подтверждение заявок не требуется
        if (eventDto.getParticipantLimit() < 1 || !eventDto.getRequestModeration()) {
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

            if (confirmedRequestCount >= eventDto.getParticipantLimit()) {
                // нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие (Ожидается код ошибки 409)
                throw new ConflictException("The participant limit has been reached for event " + eventId, "Forbidden action");
            } else if (updateRequestDto.getRequestIds().size() < eventDto.getParticipantLimit() - confirmedRequestCount) {
                requestsToConfirm = updateRequestDto.getRequestIds();
                requestRepository.updateStatusByIds(requestsToConfirm, ParticipationRequestStatus.CONFIRMED);
            } else {
                long freeSeats = eventDto.getParticipantLimit() - confirmedRequestCount;
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

    @Transactional(readOnly = true)
    public Map<Long, Long> getConfirmedRequestsByEventIds(Collection<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) return Map.of();
        return requestRepository.getConfirmedRequestsByEventIds(eventIds).stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));
    }

}