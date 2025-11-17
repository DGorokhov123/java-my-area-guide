package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.event.EventAdminApi;
import ru.practicum.dto.event.EventAdminParams;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.State;
import ru.practicum.dto.event.UpdateEventDto;
import ru.practicum.event.service.EventAdminService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class EventAdminController implements EventAdminApi {

    private final EventAdminService eventAdminService;

    // Поиск событий
    @Override
    public Collection<EventFullDto> getAllEventsByParams(List<Long> users, List<State> states, List<Long> categories,
                                                         LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        EventAdminParams params = EventAdminParams.builder()
                .users(users)
                .states(states)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build();
        return eventAdminService.getAllEventsByParams(params);
    }

    // Редактирование данных события и его статуса (отклонение/публикация).
    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventDto updateEventDto) {
        return eventAdminService.updateEventByAdmin(eventId, updateEventDto);
    }

}