package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.event.EventPublicApi;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventParams;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.EventSort;
import ru.practicum.event.service.EventPublicService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class EventPublicController implements EventPublicApi {

    private final EventPublicService eventPublicService;

    // Получение событий с возможностью фильтрации
    @Override
    public List<EventShortDto> getAllEventsByParams(String text, List<Long> categories, Boolean paid,
                                                    LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                    Boolean onlyAvailable, EventSort eventSort, Long from,
                                                    Long size, HttpServletRequest request) {
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
        return eventPublicService.getAllEventsByParams(params, request);
    }

    // Получение подробной информации об опубликованном событии по его идентификатору
    @Override
    public EventFullDto getInformationAboutEventByEventId(Long id, HttpServletRequest request) {
        return eventPublicService.getEventById(id, request);
    }

}