package ru.practicum.event.service;

import ru.practicum.category.dal.Category;
import ru.practicum.category.service.CategoryMapper;
import ru.practicum.dto.event.*;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.event.dal.Event;

import java.time.LocalDateTime;

public class EventMapper {


    public static Event toNewEvent(
            NewEventDto newEventDto,
            Long userId,
            Category category
    ) {
        return Event.builder()
                .initiatorId(userId)
                .category(category)
                .title(newEventDto.getTitle())
                .annotation(newEventDto.getAnnotation())
                .description(newEventDto.getDescription())
                .state(State.PENDING)
                .location(LocationMapper.toEntity(newEventDto.getLocation()))
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration())
                .paid(newEventDto.getPaid())
                .eventDate(newEventDto.getEventDate())
                .createdOn(LocalDateTime.now())
                .build();
    }


    public static EventFullDto toEventFullDto(
            Event event,
            UserShortDto userShortDto,
            Long confirmedRequests,
            Long views
    ) {
        if (confirmedRequests == null) confirmedRequests = 0L;
        return EventFullDto.builder()
                .id(event.getId())
                .initiator(userShortDto)
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .description(event.getDescription())
                .state(event.getState())
                .location(LocationMapper.toDto(event.getLocation()))
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .paid(event.getPaid())
                .eventDate(event.getEventDate())
                .publishedOn(event.getPublishedOn())
                .createdOn(event.getCreatedOn())
                .confirmedRequests(confirmedRequests)
                .views(views)
                .build();
    }

    public static EventShortDto toEventShortDto(
            Event event,
            UserShortDto userShortDto,
            Long confirmedRequests,
            Long views
    ) {
        if (confirmedRequests == null) confirmedRequests = 0L;
        if (views == null) views = 0L;
        return EventShortDto.builder()
                .id(event.getId())
                .initiator(userShortDto)
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .paid(event.getPaid())
                .eventDate(event.getEventDate())
                .confirmedRequests(confirmedRequests)
                .views(views)
                .build();
    }

    public static EventCommentDto toEventComment(Event event) {
        return EventCommentDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .state(event.getState())
                .build();
    }
}
