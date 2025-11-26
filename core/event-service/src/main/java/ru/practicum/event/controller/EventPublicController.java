package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.event.EventPublicApi;
import ru.practicum.dto.event.*;
import ru.practicum.event.service.EventPublicService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class EventPublicController implements EventPublicApi {

    private final EventPublicService eventPublicService;

    // Получение событий с возможностью фильтрации
    @Override
    public List<EventShortDto> getAllEventsByParams(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            EventSort eventSort,
            Integer from,
            Integer size,
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
        return eventPublicService.getAllEventsByParams(params, request);
    }

    // Получение подробной информации об опубликованном событии по его идентификатору
    @Override
    public EventFullDto getInformationAboutEventByEventId(Long userId, Long eventId, HttpServletRequest request) {
        return eventPublicService.getEventById(userId, eventId, request);
    }

    @Override
    public EventCommentDto getEventCommentDto(Long eventId) {
        return eventPublicService.getEventCommentDto(eventId);
    }

    @Override
    public Collection<EventCommentDto> getEventCommentDtoList(Collection<Long> eventIds) {
        return eventPublicService.getEventCommentDtoList(eventIds);
    }

    @Override
    public EventInteractionDto getEventInteractionDto(Long eventId) {
        return eventPublicService.getEventInteractionDto(eventId);
    }

    @Override
    public Collection<EventShortDto> getRecommendations(Long userId, Integer size) {
        return eventPublicService.getRecommendations(userId, size);
    }

    @Override
    public String sendLike(Long userId, Long eventId) {
        return eventPublicService.sendLike(userId, eventId);
    }

}