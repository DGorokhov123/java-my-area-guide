package ru.practicum.ewm.client;

import ru.practicum.EventHitDto;
import ru.practicum.EventStatsResponseDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface StatClient {

    void hit(EventHitDto eventHitDto);

    Collection<EventStatsResponseDto> stats(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            Boolean unique
    );

}
