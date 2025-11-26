package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.practicum.client.RequestClientHelper;
import ru.practicum.client.UserClientHelper;
import ru.practicum.dto.event.*;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.event.dal.Event;
import ru.practicum.event.dal.EventRepository;
import ru.practicum.event.dal.JpaSpecifications;
import ru.practicum.ewm.client.StatClient;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventPublicServiceImpl implements EventPublicService {

    private final TransactionTemplate transactionTemplate;
    private final EventRepository eventRepository;

    private final UserClientHelper userClientHelper;
    private final RequestClientHelper requestClientHelper;

    private final StatClient statClient;

    // Получение событий с возможностью фильтрации
    @Override
    public List<EventShortDto> getAllEventsByParams(EventParams params, HttpServletRequest request) {
        if (params.getRangeStart() != null && params.getRangeEnd() != null && params.getRangeEnd().isBefore(params.getRangeStart()))
            throw new BadRequestException("rangeStart should be before rangeEnd");

        // если в запросе не указан диапазон дат, то нужно выгружать события, которые произойдут позже текущего времени
        if (params.getRangeStart() == null) {
            params.setRangeStart(LocalDateTime.now());
            params.setRangeEnd(null);
        }

        List<Event> events = transactionTemplate.execute(status -> {
            PageRequest pageRequest = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
            return eventRepository.findAll(JpaSpecifications.publicFilters(params), pageRequest).getContent();
        });
        if (events == null) return List.of();

        Set<Long> userIds = events.stream().map(Event::getInitiatorId).collect(Collectors.toSet());
        Map<Long, UserShortDto> userMap = userClientHelper.retrieveUserShortDtoMapByUserIdList(userIds);

        // информация о каждом событии должна включать в себя количество просмотров и количество уже одобренных заявок на участие
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Long> confirmedRequestsMap = requestClientHelper.retrieveConfirmedRequestsMapByEventIdList(eventIds);
        Map<Long, Double> ratingMap = statClient.getRatingsByEventIdList(eventIds);

        if (params.getOnlyAvailable() == true && !confirmedRequestsMap.isEmpty()) {
            events = events.stream()
                    .filter(e -> {
                        if (Objects.equals(e.getParticipantLimit(), 0L)) return true;
                        Long confirmedRequests = confirmedRequestsMap.get(e.getId());
                        if (confirmedRequests == null) return true;
                        return confirmedRequests < e.getParticipantLimit();
                    }).toList();
        }

        List<EventShortDto> unsortedResult = events.stream()
                .map(e -> EventMapper.toEventShortDto(
                        e,
                        userMap.get(e.getInitiatorId()),
                        confirmedRequestsMap.get(e.getId()),
                        ratingMap.get(e.getId())
                ))
                .toList();

        Comparator<EventShortDto> resultComparator = switch (params.getEventSort()) {
            case VIEWS, RATING -> Comparator.comparing(EventShortDto::getRating).reversed();
            default -> Comparator.comparing(EventShortDto::getEventDate).reversed();
        };

        return unsortedResult.stream()
                .sorted(resultComparator)
                .toList();
    }

    // Получение подробной информации об опубликованном событии по его идентификатору
    @Override
    public EventFullDto getEventById(Long userId, Long eventId, HttpServletRequest request) {
        Event event = transactionTemplate.execute(status -> {
            // событие должно быть опубликовано
            return eventRepository.findByIdAndState(eventId, State.PUBLISHED)
                    .orElseThrow(() -> new NotFoundException("Event not found"));
        });

        UserShortDto userShortDto = userClientHelper.retrieveUserShortDtoByUserId(event.getInitiatorId());
        // информация о событии должна включать в себя количество просмотров и количество подтвержденных запросов
        Map<Long, Long> confirmedRequestsMap = requestClientHelper.retrieveConfirmedRequestsMapByEventIdList(List.of(eventId));
        Map<Long, Double> ratingMap = statClient.getRatingsByEventIdList(List.of(eventId));

        // информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики
        statClient.sendView(userId, eventId);

        return EventMapper.toEventFullDto(event, userShortDto, confirmedRequestsMap.get(eventId), ratingMap.get(eventId));
    }

    @Override
    @Transactional(readOnly = true)
    public EventCommentDto getEventCommentDto(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Not found Event " + eventId));
        return EventMapper.toEventComment(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<EventCommentDto> getEventCommentDtoList(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        List<Event> events = eventRepository.findAllById(ids);
        return events.stream()
                .map(EventMapper::toEventComment)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventInteractionDto getEventInteractionDto(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Not found Event " + eventId));
        return EventMapper.toInteractionDto(event);
    }

    @Override
    public Collection<EventShortDto> getRecommendations(Long userId, Integer size) {
        Map<Long, Double> recommendationMap = statClient.getUserRecommendations(userId, size);
        if (recommendationMap.isEmpty()) return List.of();

        List<Event> events = transactionTemplate.execute(status -> {
            return eventRepository.findAllById(recommendationMap.keySet());
        });
        if (events == null || events.isEmpty()) return List.of();

        Set<Long> userIds = events.stream().map(Event::getInitiatorId).collect(Collectors.toSet());
        Map<Long, UserShortDto> userMap = userClientHelper.retrieveUserShortDtoMapByUserIdList(userIds);

        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Long> confirmedRequestsMap = requestClientHelper.retrieveConfirmedRequestsMapByEventIdList(eventIds);

        return events.stream()
                .map(e -> EventMapper.toEventShortDto(
                        e,
                        userMap.get(e.getInitiatorId()),
                        confirmedRequestsMap.get(e.getId()),
                        recommendationMap.get(e.getId())
                ))
                .sorted(Comparator.comparing(EventShortDto::getRating).reversed())
                .toList();
    }

    @Override
    public String sendLike(Long userId, Long eventId) {
        if (!requestClientHelper.passedParticipationCheck(userId, eventId))
            throw new BadRequestException("User " + userId + " tries to like event " + eventId + " in which he did not participate");
        return statClient.sendLike(userId, eventId);
    }

}