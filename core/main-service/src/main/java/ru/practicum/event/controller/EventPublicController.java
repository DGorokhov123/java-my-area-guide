package ru.practicum.event.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventParams;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.EventSort;
import ru.practicum.event.service.EventPublicService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
@Slf4j
public class EventPublicController {

    private final EventPublicService eventPublicService;

    // Получение событий с возможностью фильтрации
    @GetMapping
    List<EventShortDto> getAllEventsByParams(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(defaultValue = "EVENT_DATE") EventSort eventSort,
            @RequestParam(defaultValue = "0") Long from,
            @RequestParam(defaultValue = "10") Long size,
            HttpServletRequest request
    ) {
        EventParams params = EventParams.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .eventSort(eventSort)
                .from(from)
                .size(size)
                .build();
        log.info("Calling to endpoint /events GetMapping for params: " + params.toString());
        return eventPublicService.getAllEventsByParams(params, request);
    }

    // Получение подробной информации об опубликованном событии по его идентификатору
    @GetMapping("/{id}")
    EventFullDto getInformationAboutEventByEventId(
            @PathVariable @Positive Long id,
            HttpServletRequest request
    ) {
        log.info("Calling to endpoint /events/{id} GetMapping for eventId: " + id);
        return eventPublicService.getEventById(id, request);
    }

}
