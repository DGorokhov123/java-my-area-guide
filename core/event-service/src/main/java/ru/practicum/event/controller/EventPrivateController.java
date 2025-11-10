package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.event.EventPrivateApi;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventDto;
import ru.practicum.event.service.EventPrivateService;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@Validated
public class EventPrivateController implements EventPrivateApi {

    private final EventPrivateService eventPrivateService;

    // Добавление нового события
    @Override
    public EventFullDto addNewEventByUser(Long userId, NewEventDto newEventDto) {
        return eventPrivateService.addEvent(userId, newEventDto);
    }

    // Получение событий, добавленных текущим пользователем
    @Override
    public Collection<EventShortDto> getAllEventsByUserId(Long userId, Integer from, Integer size) {
        return eventPrivateService.getEventsByUserId(userId, from, size);
    }

    // Получение полной информации о событии добавленном текущим пользователем
    @Override
    public EventFullDto getEventByUserIdAndEventId(Long userId, Long eventId) {
        return eventPrivateService.getEventByUserIdAndEventId(userId, eventId);
    }

    // Изменение события добавленного текущим пользователем
    @Override
    public EventFullDto updateEventByUserIdAndEventId(Long userId, Long eventId, UpdateEventDto updateEventDto) {
        return eventPrivateService.updateEventByUserIdAndEventId(userId, eventId, updateEventDto);
    }

}