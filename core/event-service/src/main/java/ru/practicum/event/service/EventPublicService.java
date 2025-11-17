package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.event.*;

import java.util.Collection;
import java.util.List;

public interface EventPublicService {

    List<EventShortDto> getAllEventsByParams(EventParams eventParams, HttpServletRequest request);

    EventFullDto getEventById(Long id, HttpServletRequest request);

    EventCommentDto getEventCommentDto(Long id);

    Collection<EventCommentDto> getEventCommentDtoList(Collection<Long> ids);

    EventInteractionDto getEventInteractionDto(Long id);

}
