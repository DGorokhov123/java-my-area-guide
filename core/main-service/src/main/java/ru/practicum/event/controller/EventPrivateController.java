package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventDto;
import ru.practicum.event.service.EventPrivateService;

import java.util.Collection;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
@Validated
public class EventPrivateController {

    private final EventPrivateService eventPrivateService;

    // Добавление нового события
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    EventFullDto addNewEventByUser(
            @PathVariable @Positive Long userId,
            @Valid @RequestBody NewEventDto newEventDto
    ) {
        log.info("Calling to endpoint /users/{userId}/events PostMapping for userId: " + userId);
        return eventPrivateService.addEvent(userId, newEventDto);
    }

    // Получение событий, добавленных текущим пользователем
    @GetMapping
    Collection<EventShortDto> getAllEventsByUserId(
            @PathVariable @Positive Long userId,
            @RequestParam(defaultValue = "0") Long from,
            @RequestParam(defaultValue = "10") Long size
    ) {
        log.info("Calling to endpoint /users/{userId}/events GetMapping for userId: " + userId);
        return eventPrivateService.getEventsByUserId(userId, from, size);
    }

    // Получение полной информации о событии добавленном текущим пользователем
    @GetMapping("/{eventId}")
    EventFullDto getEventByUserIdAndEventId(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId
    ) {
        log.info("Calling to endpoint /users/{userId}/events/{eventId} GetMapping for userId: "
                + userId + " and eventId: " + eventId);
        return eventPrivateService.getEventByUserIdAndEventId(userId, eventId);
    }

    // Изменение события добавленного текущим пользователем
    @PatchMapping("/{eventId}")
    EventFullDto updateEventByUserIdAndEventId(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId,
            @Valid @RequestBody UpdateEventDto updateEventDto
    ) {
        log.info("Calling to endpoint /users/{userId}/events/{eventId} PatchMapping for userId: " + userId
                + " and eventId: " + eventId + "."
                + "Information by eventDto: " + updateEventDto.toString());
        return eventPrivateService.updateEventByUserIdAndEventId(userId, eventId, updateEventDto);
    }


}
