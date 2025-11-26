package ru.practicum.ewm.client;

import ru.practicum.EventHitDto;
import ru.practicum.EventStatsResponseDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface StatClient {

    void hit(EventHitDto eventHitDto);

    Collection<EventStatsResponseDto> stats(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            Boolean unique
    );

    String sendView(Long userId, Long eventId);

    String sendRegister(Long userId, Long eventId);

    String sendLike(Long userId, Long eventId);

    Map<Long, Double> getUserRecommendations(Long userId, Integer size);

    Map<Long, Double> getRatingsByEventIdList(List<Long> eventIdList);

}
