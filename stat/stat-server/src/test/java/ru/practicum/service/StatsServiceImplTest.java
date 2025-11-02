package ru.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EventHitDto;
import ru.practicum.EventStatsResponseDto;
import ru.practicum.model.Stat;
import ru.practicum.repository.StatServiceRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
class StatsServiceImplTest {

    @Autowired
    private StatsService statsService;

    @Autowired
    private StatServiceRepository statServiceRepository;

    private EventHitDto eventHitDto1;
    private EventHitDto eventHitDto2;
    private EventHitDto eventHitDto3;
    private EventHitDto eventHitDto4;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        eventHitDto1 = EventHitDto.builder()
                .app("app1")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(now.minusHours(1))
                .build();

        eventHitDto2 = EventHitDto.builder()
                .app("app1")
                .uri("/events/2")
                .ip("192.168.1.2")
                .timestamp(now.minusHours(2))
                .build();

        eventHitDto3 = EventHitDto.builder()
                .app("app2")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(now.minusHours(3))
                .build();

        eventHitDto4 = EventHitDto.builder()
                .app("app1")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(now.minusHours(4))
                .build();
    }

    @Test
    void hit_ShouldSaveEventSuccessfully() {
        // When
        statsService.hit(eventHitDto1);

        // Then
        List<Stat> savedStats = statServiceRepository.findAll();
        assertEquals(1, savedStats.size());

        Stat savedStat = savedStats.get(0);
        assertEquals(eventHitDto1.getApp(), savedStat.getApp());
        assertEquals(eventHitDto1.getUri(), savedStat.getUri());
        assertEquals(eventHitDto1.getIp(), savedStat.getIp());
        assertEquals(eventHitDto1.getTimestamp(), savedStat.getTimestamp());
    }

    @Test
    void hit_ShouldSaveMultipleEventsSuccessfully() {
        // When
        statsService.hit(eventHitDto1);
        statsService.hit(eventHitDto2);
        statsService.hit(eventHitDto3);

        // Then
        List<Stat> savedStats = statServiceRepository.findAll();
        assertEquals(3, savedStats.size());
    }

    @Test
    void getStats_ShouldThrowExceptionWhenStartIsAfterEnd() {
        // Given
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.minusHours(1);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> statsService.getStats(start, end, null, false)
        );
    }

    @Test
    void getStats_ShouldReturnStatsWithUniqueIpWhenUrisAreEmptyAndUniqueIsTrue() {
        // Given
        statsService.hit(eventHitDto1);
        statsService.hit(eventHitDto2);
        statsService.hit(eventHitDto3);
        statsService.hit(eventHitDto4);
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        // When
        Collection<EventStatsResponseDto> result = statsService.getStats(start, end, null, true);

        // Then
        assertEquals(3, result.size());

        EventStatsResponseDto[] statsArray = result.toArray(new EventStatsResponseDto[0]);

        // Проверяем, что результат отсортирован по убыванию hits
        assertTrue(statsArray[0].getHits() >= statsArray[1].getHits());
        assertTrue(statsArray[1].getHits() >= statsArray[2].getHits());
    }

    @Test
    void getStats_ShouldReturnStatsWithoutUniqueIpWhenUrisAreEmptyAndUniqueIsFalse() {
        // Given
        statsService.hit(eventHitDto1);
        statsService.hit(eventHitDto2);
        statsService.hit(eventHitDto3);
        statsService.hit(eventHitDto4);
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        // When
        Collection<EventStatsResponseDto> result = statsService.getStats(start, end, null, false);

        // Then
        assertEquals(3, result.size());

        // Проверяем, что учитываются все хиты, включая повторы IP
        long totalHits = result.stream().mapToLong(EventStatsResponseDto::getHits).sum();
        assertEquals(4, totalHits);
    }

    @Test
    void getStats_ShouldReturnStatsWithUniqueIpForSpecificUris() {
        // Given
        statsService.hit(eventHitDto1);
        statsService.hit(eventHitDto2);
        statsService.hit(eventHitDto3);
        statsService.hit(eventHitDto4);
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        List<String> uris = List.of("/events/1");

        // When
        Collection<EventStatsResponseDto> result = statsService.getStats(start, end, uris, true);

        // Then
        assertEquals(2, result.size());

        // Проверяем, что возвращаются только статистики для указанных URI
        result.forEach(stat -> assertEquals("/events/1", stat.getUri()));
    }

    @Test
    void getStats_ShouldReturnStatsWithoutUniqueIpForSpecificUris() {
        // Given
        statsService.hit(eventHitDto1);
        statsService.hit(eventHitDto2);
        statsService.hit(eventHitDto3);
        statsService.hit(eventHitDto4);
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        List<String> uris = List.of("/events/1", "/events/2");

        // When
        Collection<EventStatsResponseDto> result = statsService.getStats(start, end, uris, false);

        // Then
        assertEquals(3, result.size());

        // Проверяем, что возвращаются только статистики для указанных URI
        result.forEach(stat ->
                assertTrue(stat.getUri().equals("/events/1") || stat.getUri().equals("/events/2"))
        );
    }

    @Test
    void getStats_ShouldReturnEmptyListWhenNoDataInTimeRange() {
        // Given
        statsService.hit(eventHitDto1);
        statsService.hit(eventHitDto2);
        statsService.hit(eventHitDto3);
        statsService.hit(eventHitDto4);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        // When
        Collection<EventStatsResponseDto> result = statsService.getStats(start, end, null, false);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void getStats_ShouldReturnEmptyListWhenNoMatchingUris() {
        // Given
        statsService.hit(eventHitDto1);
        statsService.hit(eventHitDto2);
        statsService.hit(eventHitDto3);
        statsService.hit(eventHitDto4);
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        List<String> uris = List.of("/events/999");

        // When
        Collection<EventStatsResponseDto> result = statsService.getStats(start, end, uris, false);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void getStats_ShouldHandleEmptyUrisList() {
        // Given
        statsService.hit(eventHitDto1);
        statsService.hit(eventHitDto2);
        statsService.hit(eventHitDto3);
        statsService.hit(eventHitDto4);
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        List<String> emptyUris = List.of();

        // When
        Collection<EventStatsResponseDto> result = statsService.getStats(start, end, emptyUris, false);

        // Then
        assertEquals(3, result.size());
    }

    @Test
    void getStats_ShouldCorrectlyCountUniqueIps() {
        // Given
        // Создаем несколько хитов с одинаковыми IP для одного URI
        EventHitDto hit1 = EventHitDto.builder()
                .app("app1")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(LocalDateTime.now().minusHours(1))
                .build();

        EventHitDto hit2 = EventHitDto.builder()
                .app("app1")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(LocalDateTime.now().minusHours(2))
                .build();

        EventHitDto hit3 = EventHitDto.builder()
                .app("app1")
                .uri("/events/1")
                .ip("192.168.1.2")
                .timestamp(LocalDateTime.now().minusHours(3))
                .build();

        statsService.hit(hit1);
        statsService.hit(hit2);
        statsService.hit(hit3);

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        // When - уникальные IP
        Collection<EventStatsResponseDto> uniqueResult = statsService.getStats(start, end, null, true);

        // Then
        assertEquals(1, uniqueResult.size());
        EventStatsResponseDto uniqueStat = uniqueResult.iterator().next();
        assertEquals(2L, uniqueStat.getHits()); // 2 уникальных IP

        // When - все IP (не уникальные)
        Collection<EventStatsResponseDto> allResult = statsService.getStats(start, end, null, false);

        // Then
        assertEquals(1, allResult.size());
        EventStatsResponseDto allStat = allResult.iterator().next();
        assertEquals(3L, allStat.getHits()); // 3 общих хита
    }

    @Test
    void getStats_ShouldReturnResultsSortedByHitsDesc() {
        // Given
        // Создаем данные с разным количеством хитов
        EventHitDto hit1 = EventHitDto.builder()
                .app("app1")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(LocalDateTime.now().minusHours(1))
                .build();

        EventHitDto hit2 = EventHitDto.builder()
                .app("app1")
                .uri("/events/2")
                .ip("192.168.1.1")
                .timestamp(LocalDateTime.now().minusHours(2))
                .build();

        EventHitDto hit3 = EventHitDto.builder()
                .app("app1")
                .uri("/events/2")
                .ip("192.168.1.2")
                .timestamp(LocalDateTime.now().minusHours(3))
                .build();

        EventHitDto hit4 = EventHitDto.builder()
                .app("app1")
                .uri("/events/2")
                .ip("192.168.1.3")
                .timestamp(LocalDateTime.now().minusHours(4))
                .build();

        statsService.hit(hit1);
        statsService.hit(hit2);
        statsService.hit(hit3);
        statsService.hit(hit4);

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        // When
        Collection<EventStatsResponseDto> result = statsService.getStats(start, end, null, true);

        // Then
        EventStatsResponseDto[] statsArray = result.toArray(new EventStatsResponseDto[0]);
        assertEquals(2, statsArray.length);

        // /events/2 должен быть первым (3 уникальных IP)
        assertEquals("/events/2", statsArray[0].getUri());
        assertEquals(3L, statsArray[0].getHits());

        // /events/1 должен быть вторым (1 уникальный IP)
        assertEquals("/events/1", statsArray[1].getUri());
        assertEquals(1L, statsArray[1].getHits());
    }

}