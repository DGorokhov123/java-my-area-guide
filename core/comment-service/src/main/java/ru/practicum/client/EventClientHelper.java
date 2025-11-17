package ru.practicum.client;

import org.springframework.stereotype.Component;
import ru.practicum.api.event.EventAllApi;

@Component
public class EventClientHelper extends EventClientAbstractHelper {

    public EventClientHelper(EventAllApi eventApiClient) {
        super(eventApiClient);
    }

}
