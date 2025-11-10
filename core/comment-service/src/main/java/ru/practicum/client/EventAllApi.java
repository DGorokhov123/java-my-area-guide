package ru.practicum.client;

import ru.practicum.api.event.EventAdminApi;
import ru.practicum.api.event.EventPrivateApi;
import ru.practicum.api.event.EventPublicApi;

public interface EventAllApi extends EventPublicApi, EventPrivateApi, EventAdminApi {
}
