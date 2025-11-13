package ru.practicum.api.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.State;
import ru.practicum.dto.event.UpdateEventDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface EventAdminApi {

    // Поиск событий
    @GetMapping("/admin/events")
    @ResponseStatus(HttpStatus.OK)
    Collection<EventFullDto> getAllEventsByParams(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<State> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size
    );

    // Редактирование данных события и его статуса (отклонение/публикация).
    @PatchMapping("/admin/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    EventFullDto updateEventByAdmin(
            @PathVariable Long eventId,
            @RequestBody @Valid UpdateEventDto updateEventDto
    );

}
