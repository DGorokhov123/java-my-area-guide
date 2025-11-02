package ru.practicum.client;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.EventHitDto;
import ru.practicum.EventStatsResponseDto;
import ru.practicum.ewm.client.StatClient;

import java.time.LocalDateTime;
import java.util.Collection;

@Disabled("Выполнять только при запущенных Discovery and Config servers")
@AutoConfigureTestDatabase
@SpringBootTest
public class StatClientTest {

    @Autowired
    StatClient statClient;

    @Test
    void simpleClientTest() {
        System.out.println(1);
        LocalDateTime now = LocalDateTime.now();
        EventHitDto hit = EventHitDto.builder()
                .app("app1")
                .uri("/events/2")
                .ip("192.168.1.2")
                .timestamp(now.minusHours(2))
                .build();
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        statClient.hit(hit);

        Collection<EventStatsResponseDto> eventStatsResponseDtoCollection = statClient.stats(start, end, null, true);
        System.out.println(eventStatsResponseDtoCollection.size() + " уникальных:");
        for (EventStatsResponseDto ev : eventStatsResponseDtoCollection) System.out.println(ev);

        Collection<EventStatsResponseDto> eventStatsResponseDtoCollection2 = statClient.stats(start, end, null, false);
        System.out.println(eventStatsResponseDtoCollection2.size() + " всего:");
        for (EventStatsResponseDto ev : eventStatsResponseDtoCollection2) System.out.println(ev);

    }

}
