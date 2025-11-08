package ru.practicum.api.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventDto;

import java.util.Collection;

public interface EventPrivateApi {

    // Добавление нового события
    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    EventFullDto addNewEventByUser(
            @PathVariable @Positive Long userId,
            @Valid @RequestBody NewEventDto newEventDto
    );

    // Получение событий, добавленных текущим пользователем
    @GetMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.OK)
    Collection<EventShortDto> getAllEventsByUserId(
            @PathVariable @Positive Long userId,
            @RequestParam(defaultValue = "0") Long from,
            @RequestParam(defaultValue = "10") Long size
    );

    // Получение полной информации о событии добавленном текущим пользователем
    @GetMapping("/users/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    EventFullDto getEventByUserIdAndEventId(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId
    );

    // Изменение события добавленного текущим пользователем
    @PatchMapping("/users/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    EventFullDto updateEventByUserIdAndEventId(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId,
            @Valid @RequestBody UpdateEventDto updateEventDto
    );

}
