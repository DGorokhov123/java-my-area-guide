package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import ru.practicum.category.dal.Category;
import ru.practicum.category.dal.CategoryRepository;
import ru.practicum.client.RequestClientHelper;
import ru.practicum.client.UserClientHelper;
import ru.practicum.dto.event.*;
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

    private final TransactionTemplate transactionTemplate;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final ViewRepository viewRepository;

    private final UserClientHelper userClientHelper;
    private final RequestClientHelper requestClientHelper;

    // Добавление нового события
    @Override
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        UserShortDto userShortDto = userClientHelper.retrieveUserShortDtoByUserIdOrFall(userId);

        return transactionTemplate.execute(status -> {
            Category category = categoryRepository.findById(newEventDto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Not found Category " + newEventDto.getCategory()));

            Event newEvent = EventMapper.toNewEvent(newEventDto, userId, category);
            eventRepository.save(newEvent);
            return EventMapper.toEventFullDto(newEvent, userShortDto, 0L, 0L);
        });
    }

    // Получение полной информации о событии добавленном текущим пользователем
    @Override
    public EventFullDto getEventByUserIdAndEventId(Long userId, Long eventId) {
        Event event = transactionTemplate.execute(status -> {
            return eventRepository.findById(eventId)
                    .orElseThrow(() -> new NotFoundException("Not found Event " + eventId));
        });

        if (!Objects.equals(userId, event.getInitiatorId()))
            throw new ConflictException("User " + userId + " is not an initiator of event " + eventId, "Forbidden action");

        Long views = transactionTemplate.execute(status -> {
            return viewRepository.countByEventId(eventId);
        });

        UserShortDto userShortDto = userClientHelper.retrieveUserShortDtoByUserId(userId);
        Map<Long, Long> confirmedRequestsMap = requestClientHelper.retrieveConfirmedRequestsMapByEventIdList(List.of(eventId));

        return EventMapper.toEventFullDto(event, userShortDto, confirmedRequestsMap.get(eventId), views);
    }

    // Получение событий, добавленных текущим пользователем
    @Override
    public List<EventShortDto> getEventsByUserId(Long userId, Integer from, Integer size) {
        UserShortDto userShortDto = userClientHelper.retrieveUserShortDtoByUserId(userId);

        List<Event> events = transactionTemplate.execute(status -> {
            Pageable pageable = PageRequest.of(from / size, size, Sort.by("eventDate").descending());
            return eventRepository.findByInitiatorId(userId, pageable);
        });
        if (events == null || events.isEmpty()) return List.of();

        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Long> confirmedRequestsMap = requestClientHelper.retrieveConfirmedRequestsMapByEventIdList(eventIds);

        Map<Long, Long> viewsMap = transactionTemplate.execute(status -> {
            return viewRepository.countsByEventIds(eventIds)
                    .stream()
                    .collect(Collectors.toMap(
                            r -> (Long) r[0],
                            r -> (Long) r[1]
                    ));
        });

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
    public EventFullDto updateEventByUserIdAndEventId(Long userId, Long eventId, UpdateEventDto updateEventDto) {
        UserShortDto userShortDto = userClientHelper.retrieveUserShortDtoByUserId(userId);
        Map<Long, Long> confirmedRequestsMap = requestClientHelper.retrieveConfirmedRequestsMapByEventIdList(List.of(eventId));

        return transactionTemplate.execute(status -> {
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

            Long views = viewRepository.countByEventId(eventId);
            return EventMapper.toEventFullDto(event, userShortDto, confirmedRequestsMap.get(eventId), views);
        });
    }

}