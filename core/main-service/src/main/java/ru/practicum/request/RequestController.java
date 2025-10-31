package ru.practicum.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.EventRequestStatusUpdateRequestDto;
import ru.practicum.request.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@Validated
public class RequestController {

    private final RequestService requestService;

    // ЗАЯВКИ ТЕКУЩЕГО ПОЛЬЗОВАТЕЛЯ

    // Добавление запроса от текущего пользователя на участие в событии
    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addRequest(
            @PathVariable @Positive(message = "User Id not valid") Long userId,
            @RequestParam @Positive(message = "Event Id not valid") Long eventId
    ) {
        return requestService.addRequest(userId, eventId);
    }

    // Отмена своего запроса на участие в событии
    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(
            @PathVariable @Positive(message = "User Id not valid") Long userId,
            @PathVariable @Positive(message = "Request Id not valid") Long requestId
    ) {
        return requestService.cancelRequest(userId, requestId);
    }

    // Получение информации о заявках текущего пользователя на участие в чужих событиях
    @GetMapping("/users/{userId}/requests")
    public Collection<ParticipationRequestDto> getRequesterRequests(
            @PathVariable @Positive(message = "User Id not valid") Long userId
    ) {
        return requestService.findRequesterRequests(userId);
    }

    // ЗАЯВКИ НА КОНКРЕТНОЕ СОБЫТИЕ

    // Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя
    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResultDto moderateRequest(
            @PathVariable @Positive(message = "User Id not valid") Long userId,
            @PathVariable @Positive(message = "Event Id not valid") Long eventId,
            @RequestBody @Valid EventRequestStatusUpdateRequestDto updateRequestDto
    ) {
        return requestService.moderateRequest(userId, eventId, updateRequestDto);
    }

    // Получение информации о запросах на участие в событии текущего пользователя
    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public Collection<ParticipationRequestDto> getEventRequests(
            @PathVariable @Positive(message = "User Id not valid") Long userId,
            @PathVariable @Positive(message = "Event Id not valid") Long eventId
    ) {
        return requestService.findEventRequests(userId, eventId);
    }


}
