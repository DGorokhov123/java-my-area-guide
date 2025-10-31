package ru.practicum.event.service;

import ru.practicum.event.dto.EventAdminParams;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventDto;

import java.util.List;

public interface EventAdminService {
    List<EventFullDto> getAllEventsByParams(EventAdminParams eventAdminParams);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventDto updateEventDto);
}
