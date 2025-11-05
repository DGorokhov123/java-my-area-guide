package ru.practicum.service;

import ru.practicum.EventHitDto;
import ru.practicum.EventStatsResponseDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface StatsService {

    void hit(EventHitDto eventHitDto);

    Collection<EventStatsResponseDto> getStats(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            boolean isUnique
    );

}
