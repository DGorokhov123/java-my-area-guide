package ru.practicum.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.api.request.RequestApi;

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

}