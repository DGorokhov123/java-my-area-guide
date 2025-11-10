package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.api.user.UserApi;

@FeignClient(name = "user-service")
public interface UserClient extends UserApi {
}
