package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.event.*;

import java.util.Collection;
import java.util.List;

public interface EventPublicService {

    List<EventShortDto> getAllEventsByParams(EventParams eventParams, HttpServletRequest request);

    EventFullDto getEventById(Long userId, Long eventId, HttpServletRequest request);

    EventCommentDto getEventCommentDto(Long id);

    Collection<EventCommentDto> getEventCommentDtoList(Collection<Long> ids);

    EventInteractionDto getEventInteractionDto(Long id);

    Collection<EventShortDto> getRecommendations(Long userId, Integer size);

    String sendLike(Long userId, Long eventId);

}
