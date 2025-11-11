package ru.practicum.event.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dal.Category;
import ru.practicum.category.dal.CategoryRepository;
import ru.practicum.client.RequestClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.ParticipationRequestStatus;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.event.dal.Event;
import ru.practicum.event.dal.EventRepository;
import ru.practicum.event.dal.ViewRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventPrivateServiceImpl implements EventPrivateService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final ViewRepository viewRepository;

    private final UserClient userClient;
    private final RequestClient requestClient;

    // Добавление нового события
    @Override
    @Transactional(readOnly = false)
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        UserShortDto userShortDto;
        try {
            userShortDto = userClient.getUserShort(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Not confirmed the existence of User " + userId);
        }

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Not found Category " + newEventDto.getCategory()));

        Event newEvent = EventMapper.toNewEvent(newEventDto, userId, category);
        eventRepository.save(newEvent);
        return EventMapper.toEventFullDto(newEvent, userShortDto, 0L, 0L);
    }

    // Получение полной информации о событии добавленном текущим пользователем
    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventByUserIdAndEventId(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Not found Event " + eventId));

        if (!Objects.equals(userId, event.getInitiatorId()))
            throw new ConflictException("User " + userId + " is not an initiator of event " + eventId, "Forbidden action");

        UserShortDto userShortDto;
        try {
            userShortDto = userClient.getUserShort(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Not confirmed the existence of User " + userId);
        }

        Map<Long, Long> confirmedRequestsMap;
        try {
            confirmedRequestsMap = requestClient.getConfirmedRequestsByEventIds(List.of(eventId));
        } catch (FeignException e) {
            throw new NotFoundException("Unable to get info about confirmed requests");
        }

        Long views = viewRepository.countByEventId(eventId);
        return EventMapper.toEventFullDto(event, userShortDto, confirmedRequestsMap.get(eventId), views);
    }

    // Получение событий, добавленных текущим пользователем
    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByUserId(Long userId, Integer from, Integer size) {
        UserShortDto userShortDto;
        try {
            userShortDto = userClient.getUserShort(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Not confirmed the existence of User " + userId);
        }
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("eventDate").descending());
        List<Event> events = eventRepository.findByInitiatorId(userId, pageable);
        List<Long> eventIds = events.stream().map(Event::getId).toList();

        Map<Long, Long> confirmedRequestsMap;
        try {
            confirmedRequestsMap = requestClient.getConfirmedRequestsByEventIds(eventIds);
        } catch (FeignException e) {
            throw new NotFoundException("Unable to get info about confirmed requests");
        }

        Map<Long, Long> viewsMap = viewRepository.countsByEventIds(eventIds)
                .stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));

        return events.stream()
                .map(e -> EventMapper.toEventShortDto(
                        e,
                        userShortDto,
                        confirmedRequestsMap.get(e.getId()),
                        viewsMap.get(e.getId())
                ))
                .toList();
    }

    // Изменение события добавленного текущим пользователем
    @Override
    @Transactional
    public EventFullDto updateEventByUserIdAndEventId(Long userId, Long eventId, UpdateEventDto updateEventDto) {
        UserShortDto userShortDto;
        try {
            userShortDto = userClient.getUserShort(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Not confirmed the existence of User " + userId);
        }
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!Objects.equals(userId, event.getInitiatorId()))
            throw new ConflictException("User " + userId + " is not an initiator of event " + eventId, "Forbidden action");

        // изменить можно только отмененные события или события в состоянии ожидания модерации (Ожидается код ошибки 409)
        if (event.getState() != State.PENDING && event.getState() != State.CANCELED)
            throw new ConflictException("Only pending or canceled events can be changed");

        // дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента (Ожидается код ошибки 409)
        if (updateEventDto.getEventDate() != null &&
                updateEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2)))
            throw new ConflictException("Event date must be at least 2 hours from now");

        // если все хорошо, изменяем обновленные данные:
        if (updateEventDto.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventDto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + updateEventDto.getCategory() + " not found"));
            event.setCategory(category);
        }
        if (updateEventDto.getTitle() != null) event.setTitle(updateEventDto.getTitle());
        if (updateEventDto.getAnnotation() != null) event.setAnnotation(updateEventDto.getAnnotation());
        if (updateEventDto.getDescription() != null) event.setDescription(updateEventDto.getDescription());
        if (updateEventDto.getLocation() != null)
            event.setLocation(LocationMapper.toEntity(updateEventDto.getLocation()));
        if (updateEventDto.getPaid() != null) event.setPaid(updateEventDto.getPaid());
        if (updateEventDto.getParticipantLimit() != null)
            event.setParticipantLimit(updateEventDto.getParticipantLimit());
        if (updateEventDto.getRequestModeration() != null)
            event.setRequestModeration(updateEventDto.getRequestModeration());
        if (updateEventDto.getEventDate() != null) event.setEventDate(updateEventDto.getEventDate());
        if (Objects.equals(updateEventDto.getStateAction(), StateAction.CANCEL_REVIEW)) {
            event.setState(State.CANCELED);
        } else if (Objects.equals(updateEventDto.getStateAction(), StateAction.SEND_TO_REVIEW)) {
            event.setState(State.PENDING);
        }

        eventRepository.save(event);

        Map<Long, Long> confirmedRequestsMap;
        try {
            confirmedRequestsMap = requestClient.getConfirmedRequestsByEventIds(List.of(eventId));
        } catch (FeignException e) {
            throw new NotFoundException("Unable to get info about confirmed requests");
        }

        Long views = viewRepository.countByEventId(eventId);
        return EventMapper.toEventFullDto(event, userShortDto, confirmedRequestsMap.get(eventId), views);
    }

}