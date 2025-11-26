package ru.practicum.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.EventRequestStatusUpdateRequestDto;
import ru.practicum.dto.request.EventRequestStatusUpdateResultDto;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.Collection;
import java.util.Map;

public interface RequestApi {

    // ЗАЯВКИ ТЕКУЩЕГО ПОЛЬЗОВАТЕЛЯ

    // Добавление запроса от текущего пользователя на участие в событии
    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    ParticipationRequestDto addRequest(
            @PathVariable @Positive(message = "User Id not valid") Long userId,
            @RequestParam @Positive(message = "Event Id not valid") Long eventId
    );

    // Отмена своего запроса на участие в событии
    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    ParticipationRequestDto cancelRequest(
            @PathVariable @Positive(message = "User Id not valid") Long userId,
            @PathVariable @Positive(message = "Request Id not valid") Long requestId
    );

    // Получение информации о заявках текущего пользователя на участие в чужих событиях
    @GetMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.OK)
    Collection<ParticipationRequestDto> getRequesterRequests(
            @PathVariable @Positive(message = "User Id not valid") Long userId
    );

    // ЗАЯВКИ НА КОНКРЕТНОЕ СОБЫТИЕ

    // Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя
    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    EventRequestStatusUpdateResultDto moderateRequest(
            @PathVariable @Positive(message = "User Id not valid") Long userId,
            @PathVariable @Positive(message = "Event Id not valid") Long eventId,
            @RequestBody @Valid EventRequestStatusUpdateRequestDto updateRequestDto
    );

    // Получение информации о запросах на участие в событии текущего пользователя
    @GetMapping("/users/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    Collection<ParticipationRequestDto> getEventRequests(
            @PathVariable @Positive(message = "User Id not valid") Long userId,
            @PathVariable @Positive(message = "Event Id not valid") Long eventId
    );

    // INTERACTION API

    // Запрос количества подтвержденных заявок по списку eventId
    @PostMapping("/requests/confirmed")
    @ResponseStatus(HttpStatus.OK)
    Map<Long, Long> getConfirmedRequestsByEventIds(
            @RequestBody Collection<Long> eventIds
    );

    // Проверка участия пользователя в конкретном событии перед лайком
    @GetMapping("/users/{userId}/events/{eventId}/check/participation")
    @ResponseStatus(HttpStatus.OK)
    String checkParticipation(
            @PathVariable @Positive(message = "User Id not valid") Long userId,
            @PathVariable @Positive(message = "Event Id not valid") Long eventId
    );

}