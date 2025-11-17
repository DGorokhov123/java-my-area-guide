package ru.practicum.client;

import org.springframework.stereotype.Component;
import ru.practicum.api.user.UserApi;

@Component
public class UserClientHelper extends UserClientAbstractHelper {

    public UserClientHelper(UserApi userApiClient) {
        super(userApiClient);
    }

}