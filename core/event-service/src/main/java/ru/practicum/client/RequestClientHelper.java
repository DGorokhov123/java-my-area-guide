package ru.practicum.client;

import org.springframework.stereotype.Component;
import ru.practicum.api.request.RequestApi;

@Component
public class RequestClientHelper extends RequestClientAbstractHelper {

    public RequestClientHelper(RequestApi requestApiClient) {
        super(requestApiClient);
    }

}