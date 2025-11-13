package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.practicum.category.dal.Category;
import ru.practicum.category.dal.CategoryRepository;
import ru.practicum.client.RequestClientHelper;
import ru.practicum.client.UserClientHelper;
import ru.practicum.dto.event.*;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.event.dal.Event;
import ru.practicum.event.dal.EventRepository;
import ru.practicum.event.dal.JpaSpecifications;
import ru.practicum.event.dal.ViewRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventAdminServiceImpl implements EventAdminService {

    private final TransactionTemplate transactionTemplate;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final ViewRepository viewRepository;

    private final RequestClientHelper requestClientHelper;
    private final UserClientHelper userClientHelper;

    // Поиск событий
    @Override
    public List<EventFullDto> getAllEventsByParams(EventAdminParams params) {
        Page<Event> events = transactionTemplate.execute(status -> {
            Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
            return eventRepository.findAll(JpaSpecifications.adminFilters(params), pageable);
        });
        if (events == null) return List.of();

        Set<Long> userIds = events.stream().map(Event::getInitiatorId).collect(Collectors.toSet());
        List<Long> eventIds = events.stream().map(Event::getId).toList();

        Map<Long, UserShortDto> userMap = userClientHelper.retrieveUserShortDtoMapByUserIdList(userIds);
        Map<Long, Long> confirmedRequestsMap = requestClientHelper.retrieveConfirmedRequestsMapByEventIdList(eventIds);

        Map<Long, Long> viewsMap = viewRepository.countsByEventIds(eventIds)
                .stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));

        return events.stream()
                .map(e -> EventMapper.toEventFullDto(
                        e,
                        userMap.get(e.getInitiatorId()),
                        confirmedRequestsMap.get(e.getId()),
                        viewsMap.get(e.getId())
                ))
                .toList();
    }

    // Редактирование данных события и его статуса (отклонение/публикация).
    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventDto updateEventDto) {
        Long initiatorId = transactionTemplate.execute(status -> {
            return eventRepository.findById(eventId)
                    .orElseThrow(() -> new NotFoundException("Not found Event " + eventId))
                    .getInitiatorId();
        });

        UserShortDto userShortDto = userClientHelper.retrieveUserShortDtoByUserId(initiatorId);
        Map<Long, Long> confirmedRequestsMap = requestClientHelper.retrieveConfirmedRequestsMapByEventIdList(List.of(eventId));

        return transactionTemplate.execute(status -> {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

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

            if (Objects.equals(updateEventDto.getStateAction(), StateAction.REJECT_EVENT)) {
                // событие можно отклонить, только если оно еще не опубликовано (Ожидается код ошибки 409)
                if (Objects.equals(event.getState(), State.PUBLISHED)) {
                    throw new ConflictException("Event in PUBLISHED state can not be rejected");
                }
                event.setState(State.CANCELED);
            } else if (Objects.equals(updateEventDto.getStateAction(), StateAction.PUBLISH_EVENT)) {
                // дата начала изменяемого события должна быть не ранее чем за час от даты публикации. (Ожидается код ошибки 409)
                if (LocalDateTime.now().plusHours(1).isAfter(event.getEventDate())) {
                    throw new ConflictException("Event time must be at least 1 hours from publish time");
                }
                // событие можно публиковать, только если оно в состоянии ожидания публикации (Ожидается код ошибки 409)
                if (!Objects.equals(event.getState(), State.PENDING)) {
                    throw new ConflictException("Event should be in PENDING state");
                }
                event.setState(State.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }

            eventRepository.save(event);

            Long views = viewRepository.countByEventId(eventId);
            return EventMapper.toEventFullDto(event, userShortDto, confirmedRequestsMap.get(eventId), views);
        });
    }

}