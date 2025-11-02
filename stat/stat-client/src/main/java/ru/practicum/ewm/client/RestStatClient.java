package ru.practicum.ewm.client;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.EventHitDto;
import ru.practicum.EventStatsResponseDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RestStatClient implements StatClient {
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    RestClient restClient;
    String statUrl;

    public RestStatClient(@Value("${explore-with-me.stat-server.url}") String statUrl) {
        this.restClient = RestClient
                .builder()
                .baseUrl(statUrl)
                .build();
        this.statUrl = statUrl;
    }

    @PostConstruct
    public void init() {
        System.out.println(statUrl);
    }

    @Override
    public void hit(EventHitDto eventHitDto) {
        try {
            restClient
                    .post()
                    .uri("/hit")
                    .body(eventHitDto)
                    .contentType(APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error("Не удалось сохранить информацию о запросе к эндпоинту: {}", e.getMessage());
        }
    }

    @Override
    public Collection<EventStatsResponseDto> stats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("/stats")
                    .queryParam("start", start.format(formatter))
                    .queryParam("end", end.format(formatter));

            if (uris != null && !uris.isEmpty()) {
                uriBuilder.queryParam("uris", String.join(",", uris));
            }
            if (unique != null) {
                uriBuilder.queryParam("unique", unique);
            }

            String uri = uriBuilder.build().toUriString();

            return restClient
                    .get()
                    .uri(uri)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Collection<EventStatsResponseDto>>() {
                    });
        } catch (RestClientException e) {
            log.error("Не удалось получить статистику по посещениям: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
