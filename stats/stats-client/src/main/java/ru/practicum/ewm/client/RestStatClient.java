package ru.practicum.ewm.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.EventHitDto;
import ru.practicum.EventStatsResponseDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Slf4j
@Component
public class RestStatClient implements StatClient {

    private final DiscoveryClient discoveryClient;
    private final Random random = new Random();
    private final DateTimeFormatter formatter;
    private final String name;

    private String statUrl;
    private RestClient restClient;
    private boolean doRenewingServerUrl = false;

    public RestStatClient(
            DiscoveryClient discoveryClient,
            @Value("${explore-with-me.stat-server.discovery.name:}") String name,
            @Value("${explore-with-me.stat-server.url:http://localhost:9090}") String url,
            @Value("${explore-with-me.stat.datetime.format}") String format
    ) {
        this.discoveryClient = discoveryClient;
        this.name = name;
        this.formatter = DateTimeFormatter.ofPattern(format);
        this.statUrl = url;
        this.restClient = RestClient.builder().baseUrl(statUrl).build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if (name == null || name.isBlank()) return;

        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(5000L);
        MaxAttemptsRetryPolicy retryPolicy = new MaxAttemptsRetryPolicy();
        retryPolicy.setMaxAttempts(10);
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
        retryTemplate.setRetryPolicy(retryPolicy);

        try {
            ServiceInstance instance = retryTemplate.execute(retryContext -> {
                List<ServiceInstance> instances =  discoveryClient.getInstances(name);
                if (instances.isEmpty()) throw new RuntimeException("try again");
                return instances.get(random.nextInt(instances.size()));
            });
            statUrl = instance.getUri().toString();
            restClient = RestClient.builder().baseUrl(statUrl).build();
            log.info("Retrieved init stat server url: {}", statUrl);
        } catch (Exception e) {
            log.warn("Discovery server error: {}", e.getMessage());
        }

        doRenewingServerUrl = true;
    }

    @Scheduled(fixedDelay = 60000)
    public void renewServerUrl() {
        if (!doRenewingServerUrl) return;
        try {
            List<String> urls = discoveryClient.getInstances(name).stream()
                    .map(i -> i.getUri().toString())
                    .toList();

            if (urls.isEmpty() || urls.contains(statUrl)) return;

            statUrl = urls.get(random.nextInt(urls.size()));
            restClient = RestClient.builder().baseUrl(statUrl).build();
            log.info("Retrieved new stat server url: {}", statUrl);
        } catch (Exception e) {
            log.warn("Discovery server error: {}", e.getMessage());
        }
    }

    @Override
    public void hit(EventHitDto eventHitDto) {
        try {
            restClient
                    .post()
                    .uri("/hit")
                    .body(eventHitDto)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("Failed call /hit on stat server: {}", e.getMessage());
        }
    }

    @Override
    public Collection<EventStatsResponseDto> stats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("/stats")
                    .queryParam("start", start.format(formatter))
                    .queryParam("end", end.format(formatter));
            if (uris != null && !uris.isEmpty())
                uriBuilder.queryParam("uris", String.join(",", uris));
            if (unique != null)
                uriBuilder.queryParam("unique", unique);
            String uri = uriBuilder.build().toUriString();
            return restClient
                    .get()
                    .uri(uri)
                    .retrieve()
                    .body(
                            new ParameterizedTypeReference<Collection<EventStatsResponseDto>>() {
                            }
                    );
        } catch (RestClientException e) {
            log.error("Failed call /stats on stat server: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public String sendView(Long userId, Long eventId) {
        throw new UnsupportedOperationException("Method sendView() is not supported");
    }

    @Override
    public String sendRegister(Long userId, Long eventId) {
        throw new UnsupportedOperationException("Method sendRegister() is not supported");
    }

    @Override
    public String sendLike(Long userId, Long eventId) {
        throw new UnsupportedOperationException("Method sendLike() is not supported");
    }

    @Override
    public Map<Long, Double> getUserRecommendations(Long userId, Integer size) {
        throw new UnsupportedOperationException("Method getUserRecommendations() is not supported");
    }

    @Override
    public Map<Long, Double> getRatingsByEventIdList(List<Long> eventIdList) {
        throw new UnsupportedOperationException("Method getRatingsByEventIdList() is not supported");
    }

}
