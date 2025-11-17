package ru.practicum.client;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.api.event.EventAllApi;
import ru.practicum.dto.event.EventCommentDto;
import ru.practicum.dto.event.EventInteractionDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ServiceInteractionException;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public abstract class EventClientAbstractHelper {

    protected final EventAllApi eventApiClient;

    // EventInteractionDto

    public EventInteractionDto retrieveEventInteractionDtoByEventIdOrFall(Long eventId) {
        try {
            return eventApiClient.getEventInteractionDto(eventId);
        } catch (RuntimeException e) {
            if (isNotFoundCode(e)) throw new NotFoundException("Not found Event " + eventId);

            log.warn("Service Interaction Error: caught " + e.getClass().getSimpleName() + " - " + e.getMessage());
            throw new ServiceInteractionException("Unable to check Event " + eventId, "event-service is unavailable");
        }
    }

    public EventInteractionDto retrieveEventInteractionDtoByEventId(Long eventId) {
        try {
            return eventApiClient.getEventInteractionDto(eventId);
        } catch (RuntimeException e) {
            if (isNotFoundCode(e)) throw new NotFoundException("Not found Event " + eventId);

            log.warn("Service Interaction Error: caught " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return EventInteractionDto.makeDummy(eventId);
        }
    }

    // EventCommentDto

    public EventCommentDto retrieveEventCommentDtoByEventIdOrFall(Long eventId) {
        try {
            return eventApiClient.getEventCommentDto(eventId);
        } catch (RuntimeException e) {
            if (isNotFoundCode(e)) throw new NotFoundException("Not found Event " + eventId);

            log.warn("Service Interaction Error: caught " + e.getClass().getSimpleName() + " - " + e.getMessage());
            throw new ServiceInteractionException("Unable to check Event " + eventId, "event-service is unavailable");
        }
    }

    public EventCommentDto retrieveEventCommentDtoByEventId(Long eventId) {
        try {
            return eventApiClient.getEventCommentDto(eventId);
        } catch (RuntimeException e) {
            if (isNotFoundCode(e)) throw new NotFoundException("Not found Event " + eventId);

            log.warn("Service Interaction Error: caught " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return EventCommentDto.makeDummy(eventId);
        }
    }

    public Map<Long, EventCommentDto> retrieveEventCommentDtoMapByUserIdList(Collection<Long> eventIdList) {
        try {
            return eventApiClient.getEventCommentDtoList(eventIdList).stream()
                    .collect(Collectors.toMap(EventCommentDto::getId, e -> e));
        } catch (RuntimeException e) {
            log.warn("Service Interaction Error: caught " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return eventIdList.stream()
                    .collect(Collectors.toMap(id -> id, EventCommentDto::makeDummy));
        }
    }

    // PRIVATE METHODS

    private boolean isNotFoundCode(RuntimeException e) {
        if (e instanceof FeignException.NotFound) return true;
        if (e.getCause() != null && e.getCause() instanceof FeignException.NotFound) return true;
        return false;
    }

}
