package ru.practicum.api.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventCommentDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.EventSort;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface EventPublicApi {

    // Получение событий с возможностью фильтрации
    @GetMapping("/events")
    @ResponseStatus(HttpStatus.OK)
    List<EventShortDto> getAllEventsByParams(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(defaultValue = "EVENT_DATE") EventSort eventSort,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request
    );

    // Получение подробной информации об опубликованном событии по его идентификатору
    @GetMapping("/events/{id}")
    @ResponseStatus(HttpStatus.OK)
    EventFullDto getInformationAboutEventByEventId(
            @PathVariable @Positive Long id,
            HttpServletRequest request
    );

    // Получение информации о событии для сервиса комментариев
    @GetMapping("/events/{id}/dto/comment")
    @ResponseStatus(HttpStatus.OK)
    EventCommentDto getEventCommentDto(
            @PathVariable @Positive Long id
    );

    // Получение информации о списке событий для сервиса комментариев
    @PostMapping("/events/dto/list/comment")
    @ResponseStatus(HttpStatus.OK)
    Collection<EventCommentDto> getEventCommentDtoList(
            @RequestBody Collection<Long> ids
    );

}