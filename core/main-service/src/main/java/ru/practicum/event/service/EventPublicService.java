package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventParams;
import ru.practicum.dto.event.EventShortDto;

import java.util.List;

public interface EventPublicService {

    List<EventShortDto> getAllEventsByParams(EventParams eventParams, HttpServletRequest request);

    EventFullDto getEventById(Long id, HttpServletRequest request);

}
