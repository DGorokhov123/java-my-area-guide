package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EventHitDto;
import ru.practicum.EventStatsResponseDto;
import ru.practicum.model.Stat;
import ru.practicum.model.mapper.StatMapper;
import ru.practicum.repository.StatServiceRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {

    private final StatServiceRepository statServiceRepository;

    @Transactional
    public void hit(EventHitDto eventHitDto) {
        log.info("Hit - invoked");
        Stat stat = statServiceRepository.save(StatMapper.INSTANCE.toStat(eventHitDto));
        log.info("Hit - stat saved successfully - {}", stat);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<EventStatsResponseDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean isUnique) {
        log.info("getStats - invoked");
        if (start.isAfter(end)) {
            log.error("Error occurred: The start date cannot be later than the end date");
            throw new IllegalArgumentException("The start date cannot be later than the end date");
        }
        if (uris == null || uris.isEmpty()) {
            if (isUnique) {
                log.info("getStats - success - unique = true, uris empty");
                return statServiceRepository.findAllByTimestampBetweenStartAndEndWithUniqueIp(start, end);
            } else {
                log.info("getStats - success - unique = false, uris empty");
                return statServiceRepository.findAllByTimestampBetweenStartAndEndWhereIpNotUnique(start, end);
            }
        } else {
            if (isUnique) {
                log.info("getStats - success - unique = true, uris not empty");
                return statServiceRepository.findAllByTimestampBetweenStartAndEndWithUrisUniqueIp(start, end, uris);
            } else {
                log.info("getStats - success - unique = false, uris not empty");
                return statServiceRepository.findAllByTimestampBetweenStartAndEndWithUrisIpNotUnique(start, end, uris);
            }
        }
    }
}