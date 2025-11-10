package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "event-service")
public interface EventClient extends EventAllApi {
}
