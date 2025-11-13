package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.practicum.EventHitDto;
import ru.practicum.client.RequestClientHelper;
import ru.practicum.client.UserClientHelper;
import ru.practicum.dto.event.*;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.event.dal.*;
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
    private final ViewRepository viewRepository;

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
            Sort sort = Sort.by(Sort.Direction.ASC, "eventDate");
            if (EventSort.VIEWS.equals(params.getEventSort())) sort = Sort.by(Sort.Direction.DESC, "views");
            PageRequest pageRequest = PageRequest.of(params.getFrom() / params.getSize(), params.getSize(), sort);
            return eventRepository.findAll(JpaSpecifications.publicFilters(params), pageRequest).getContent();
        });
        if (events == null) return List.of();

        Set<Long> userIds = events.stream().map(Event::getInitiatorId).collect(Collectors.toSet());
        List<Long> eventIds = events.stream().map(Event::getId).toList();

        Map<Long, UserShortDto> userMap = userClientHelper.retrieveUserShortDtoMapByUserIdList(userIds);
        // информация о каждом событии должна включать в себя количество просмотров и количество уже одобренных заявок на участие
        Map<Long, Long> confirmedRequestsMap = requestClientHelper.retrieveConfirmedRequestsMapByEventIdList(eventIds);

        if (params.getOnlyAvailable() == true && !confirmedRequestsMap.isEmpty()) {
            events = events.stream()
                    .filter(e -> {
                        if (Objects.equals(e.getParticipantLimit(), 0L)) return true;
                        Long confirmedRequests = confirmedRequestsMap.get(e.getId());
                        if (confirmedRequests == null) return true;
                        return confirmedRequests < e.getParticipantLimit();
                    }).toList();
        }

        Map<Long, Long> viewsMap = Optional.ofNullable(
                transactionTemplate.execute(status -> {
                    return viewRepository.countsByEventIds(eventIds)
                            .stream()
                            .collect(Collectors.toMap(
                                    r -> (Long) r[0],
                                    r -> (Long) r[1]
                            ));
                })
        ).orElse(Map.of());

        // информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики
        statClient.hit(EventHitDto.builder()
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .app("ewm-main-service")
                .timestamp(LocalDateTime.now())
                .build());

        return events.stream()
                .map(e -> EventMapper.toEventShortDto(
                        e,
                        userMap.get(e.getInitiatorId()),
                        confirmedRequestsMap.get(e.getId()),
                        viewsMap.get(e.getId())
                ))
                .toList();
    }

    // Получение подробной информации об опубликованном событии по его идентификатору
    @Override
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        Event event = transactionTemplate.execute(status -> {
            // событие должно быть опубликовано
            return eventRepository.findByIdAndState(eventId, State.PUBLISHED)
                    .orElseThrow(() -> new NotFoundException("Event not found"));
        });

        Long views = transactionTemplate.execute(status -> {
            Long viewsBefore = viewRepository.countByEventId(eventId);
            // делаем новый уникальный просмотр
            if (!viewRepository.existsByEventIdAndIp(eventId, request.getRemoteAddr())) {
                View view = View.builder()
                        .event(event)
                        .ip(request.getRemoteAddr())
                        .build();
                viewRepository.save(view);
            }
            return viewsBefore;
        });

        // информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики
        statClient.hit(EventHitDto.builder()
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .app("ewm-main-service")
                .timestamp(LocalDateTime.now())
                .build());

        UserShortDto userShortDto = userClientHelper.retrieveUserShortDtoByUserId(event.getInitiatorId());
        // информация о событии должна включать в себя количество просмотров и количество подтвержденных запросов
        Map<Long, Long> confirmedRequestsMap = requestClientHelper.retrieveConfirmedRequestsMapByEventIdList(List.of(eventId));

        return EventMapper.toEventFullDto(event, userShortDto, confirmedRequestsMap.get(eventId), views);
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

}