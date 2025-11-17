package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.practicum.client.EventClientAbstractHelper;
import ru.practicum.client.UserClientHelper;
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

    private final TransactionTemplate transactionTemplate;
    private final RequestRepository requestRepository;

    private final UserClientHelper userClientHelper;
    private final EventClientAbstractHelper eventClientHelper;

    // ЗАЯВКИ ТЕКУЩЕГО ПОЛЬЗОВАТЕЛЯ

    // Добавление запроса от текущего пользователя на участие в событии
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        userClientHelper.retrieveUserShortDtoByUserIdOrFall(userId);

        EventInteractionDto eventDto = eventClientHelper.retrieveEventInteractionDtoByEventIdOrFall(eventId);

        return transactionTemplate.execute(status -> {
            // нельзя добавить повторный запрос (Ожидается код ошибки 409)
            if (requestRepository.existsByRequesterIdAndEventId(userId, eventId))
                throw new ConflictException("User tries to make duplicate request", "Forbidden action");

            // инициатор события не может добавить запрос на участие в своём событии (Ожидается код ошибки 409)
            if (Objects.equals(userId, eventDto.getInitiatorId()))
                throw new ConflictException("User tries to request for his own event", "Forbidden action");

            // нельзя участвовать в неопубликованном событии (Ожидается код ошибки 409)
            if (eventDto.getState() != State.PUBLISHED)
                throw new ConflictException("User tries to request for non-published event", "Forbidden action");

            // если у события достигнут лимит запросов на участие - необходимо вернуть ошибку (Ожидается код ошибки 409)
            long confirmedRequestCount = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
            if (eventDto.getParticipantLimit() > 0 && confirmedRequestCount >= eventDto.getParticipantLimit())
                throw new ConflictException("Participants limit is already reached", "Forbidden action");

            // если для события отключена пре-модерация запросов на участие, то запрос должен автоматически перейти в состояние подтвержденного
            ParticipationRequestStatus newRequestStatus = ParticipationRequestStatus.PENDING;
            if (!eventDto.getRequestModeration()) newRequestStatus = ParticipationRequestStatus.CONFIRMED;
            if (Objects.equals(eventDto.getParticipantLimit(), 0L))
                newRequestStatus = ParticipationRequestStatus.CONFIRMED;

            Request newRequest = Request.builder()
                    .requesterId(userId)
                    .eventId(eventId)
                    .status(newRequestStatus)
                    .created(LocalDateTime.now())
                    .build();
            requestRepository.save(newRequest);
            return RequestMapper.toDto(newRequest);
        });
    }

    // Отмена своего запроса на участие в событии
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Not found Request " + requestId));

        if (!Objects.equals(request.getRequesterId(), userId))
            throw new ConflictException("User can cancel only his own event", "Forbidden action");

        request.setStatus(ParticipationRequestStatus.CANCELED);
        requestRepository.save(request);
        return RequestMapper.toDto(request);
    }

    // Получение информации о заявках текущего пользователя на участие в чужих событиях
    @Transactional(readOnly = true)
    public Collection<ParticipationRequestDto> findRequesterRequests(Long userId) {
        return requestRepository.findByRequesterId(userId).stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    // ЗАЯВКИ НА КОНКРЕТНОЕ СОБЫТИЕ

    // Получение информации о запросах на участие в событии, инициированном текущим пользователем
    public Collection<ParticipationRequestDto> findEventRequests(Long userId, Long eventId) {
        EventInteractionDto eventDto = eventClientHelper.retrieveEventInteractionDtoByEventIdOrFall(eventId);

        // проверка что юзер - инициатор события
        if (!Objects.equals(userId, eventDto.getInitiatorId()))
            throw new ConflictException("User " + userId + " is not an initiator of event " + eventId, "Forbidden action");

        return requestRepository.findByEventId(eventId).stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    // Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя
    public EventRequestStatusUpdateResultDto moderateRequest(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequestDto updateRequestDto
    ) {
        EventInteractionDto eventDto = eventClientHelper.retrieveEventInteractionDtoByEventIdOrFall(eventId);

        // проверка что юзер - инициатор события
        if (!Objects.equals(userId, eventDto.getInitiatorId()))
            throw new ConflictException("User " + userId + " is not an initiator of event " + eventId, "Forbidden action");

        // если для события лимит заявок равен 0 или отключена пре-модерация заявок, то подтверждение заявок не требуется
        if (eventDto.getParticipantLimit() < 1 || !eventDto.getRequestModeration())
            return new EventRequestStatusUpdateResultDto();

        return transactionTemplate.execute(status -> {
            // статус можно изменить только у заявок, находящихся в состоянии ожидания (Ожидается код ошибки 409)
            List<Request> requests = requestRepository.findAllById(updateRequestDto.getRequestIds());
            for (Request request : requests) {
                if (!Objects.equals(request.getStatus(), ParticipationRequestStatus.PENDING))
                    throw new ConflictException("Request " + request.getId() + " must have status PENDING", "Incorrectly made request");
            }

            List<Long> requestsToConfirm = new ArrayList<>();
            List<Long> requestsToReject = new ArrayList<>();

            if (Objects.equals(updateRequestDto.getStatus(), ParticipationRequestStatus.CONFIRMED)) {

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
        });
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