package ru.practicum.event.service;

import ru.practicum.dto.event.EventAdminParams;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventDto;

import java.util.List;

public interface EventAdminService {
    List<EventFullDto> getAllEventsByParams(EventAdminParams eventAdminParams);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventDto updateEventDto);
}
