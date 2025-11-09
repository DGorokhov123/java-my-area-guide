package ru.practicum.event.service;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventDto;

import java.util.List;

public interface EventPrivateService {

    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getEventByUserIdAndEventId(Long userId, Long eventId);

    List<EventShortDto> getEventsByUserId(Long userId, Integer from, Integer size);

    EventFullDto updateEventByUserIdAndEventId(Long userId, Long eventId, UpdateEventDto newEventDto);

}
