package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dal.Category;
import ru.practicum.category.dal.CategoryRepository;
import ru.practicum.dto.event.*;
import ru.practicum.event.dal.Event;
import ru.practicum.event.dal.EventRepository;
import ru.practicum.event.dal.ViewRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.dto.request.ParticipationRequestStatus;
import ru.practicum.request.dal.RequestRepository;
import ru.practicum.user.dal.User;
import ru.practicum.user.dal.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventPrivateServiceImpl implements EventPrivateService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final ViewRepository viewRepository;

    // Добавление нового события
    @Override
    @Transactional(readOnly = false)
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id=" + newEventDto.getCategory() + " was not found"));

        Event newEvent = EventMapper.toEvent(newEventDto, initiator, category);
        eventRepository.save(newEvent);
        return EventMapper.toEventFullDto(newEvent, 0L, 0L);
    }

    // Получение полной информации о событии добавленном текущим пользователем
    @Override
    public EventFullDto getEventByUserIdAndEventId(Long userId, Long eventId) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!Objects.equals(initiator.getId(), event.getInitiator().getId())) {
            throw new ConflictException("User " + userId + " is not an initiator of event " + eventId, "Forbidden action");
        }

        Long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), ParticipationRequestStatus.CONFIRMED);
        Long views = viewRepository.countByEventId(eventId);
        return EventMapper.toEventFullDto(event, confirmedRequests, views);
    }

    // Получение событий, добавленных текущим пользователем
    @Override
    public List<EventShortDto> getEventsByUserId(Long userId, Long from, Long size) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Pageable pageable = PageRequest.of(
                from.intValue() / size.intValue(),
                size.intValue(),
                Sort.by("eventDate").descending()
        );

        List<Event> events = eventRepository.findByInitiatorId(userId, pageable);
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Long> confirmedRequestsMap = requestRepository.getConfirmedRequestsByEventIds(eventIds)
                .stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));
        Map<Long, Long> viewsMap = viewRepository.countsByEventIds(eventIds)
                .stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));

        return events.stream()
                .map(e -> EventMapper.toEventShortDto(e, confirmedRequestsMap.get(e.getId()), viewsMap.get(e.getId())))
                .toList();
    }

    // Изменение события добавленного текущим пользователем
    @Override
    @Transactional(readOnly = false)
    public EventFullDto updateEventByUserIdAndEventId(Long userId, Long eventId, UpdateEventDto updateEventDto) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!Objects.equals(initiator.getId(), event.getInitiator().getId())) {
            throw new ConflictException("User " + userId + " is not an initiator of event " + eventId, "Forbidden action");
        }

        // изменить можно только отмененные события или события в состоянии ожидания модерации (Ожидается код ошибки 409)
        if (event.getState() != State.PENDING && event.getState() != State.CANCELED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        // дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента (Ожидается код ошибки 409)
        if (updateEventDto.getEventDate() != null &&
                updateEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Event date must be at least 2 hours from now");
        }

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
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), ParticipationRequestStatus.CONFIRMED);
        Long views = viewRepository.countByEventId(eventId);
        return EventMapper.toEventFullDto(event, confirmedRequests, views);
    }


}
