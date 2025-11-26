package ru.practicum.client;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.api.request.RequestApi;
import ru.practicum.exception.ServiceInteractionException;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public abstract class RequestClientAbstractHelper {

    protected final RequestApi requestApiClient;

    // Confirmed Requests Map - by EventId List
    public Map<Long, Long> retrieveConfirmedRequestsMapByEventIdList(Collection<Long> eventIdList) {
        try {
            return requestApiClient.getConfirmedRequestsByEventIds(eventIdList);
        } catch (RuntimeException e) {
            log.warn("Service Interaction Error: caught " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return eventIdList.stream().collect(Collectors.toMap(id -> id, id -> -1L));
        }
    }

    // Participation Check

    public boolean passedParticipationCheck(Long userId, Long eventId) {
        try {
            requestApiClient.checkParticipation(userId, eventId);
            return true;
        } catch (RuntimeException e) {
            if (isNotFoundCode(e)) return false;
            log.warn("Service Interaction Error: caught " + e.getClass().getSimpleName() + " - " + e.getMessage());
            throw new ServiceInteractionException("Unable to confirm participation of user " + userId + " in event " + eventId);
        }
    }

    // PRIVATE METHODS

    private boolean isNotFoundCode(RuntimeException e) {
        if (e instanceof FeignException.NotFound) return true;
        if (e.getCause() != null && e.getCause() instanceof FeignException.NotFound) return true;
        return false;
    }

}