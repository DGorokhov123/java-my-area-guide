package ru.practicum.event.service;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EventHitDto;
import ru.practicum.client.UserClient;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.ParticipationRequestStatus;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.event.dal.*;
import ru.practicum.ewm.client.StatClient;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dal.RequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventPublicServiceImpl implements EventPublicService {

    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final ViewRepository viewRepository;

    private final UserClient userClient;
    private final StatClient statClient;

    // Получение событий с возможностью фильтрации
    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getAllEventsByParams(EventParams params, HttpServletRequest request) {

        if (params.getRangeStart() != null && params.getRangeEnd() != null && params.getRangeEnd().isBefore(params.getRangeStart())) {
            throw new BadRequestException("rangeStart should be before rangeEnd");
        }

        // если в запросе не указан диапазон дат [rangeStart-rangeEnd], то нужно выгружать события, которые произойдут позже текущей даты и времени
        if (params.getRangeStart() == null) params.setRangeStart(LocalDateTime.now());

        // сортировочка и пагинация
        Sort sort = Sort.by(Sort.Direction.ASC, "eventDate");
        if (EventSort.VIEWS.equals(params.getEventSort())) sort = Sort.by(Sort.Direction.DESC, "views");
        PageRequest pageRequest = PageRequest.of(params.getFrom() / params.getSize(), params.getSize(), sort);

        Page<Event> events = eventRepository.findAll(JpaSpecifications.publicFilters(params), pageRequest);
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Set<Long> userIds = events.stream().map(Event::getInitiatorId).collect(Collectors.toSet());

        // запрашиваем список UserShortDto для заполнения поля Initiator
        Map<Long, UserShortDto> userMap;
        try {
            userMap = userClient.getUserShortDtoListByIds(userIds).stream()
                    .collect(Collectors.toMap(
                            UserShortDto::getId,
                            u -> u
                    ));
        } catch (FeignException e) {
            throw new NotFoundException("Unable to get info for Users in list " + userIds);
        }

        // информация о каждом событии должна включать в себя количество просмотров и количество уже одобренных заявок на участие
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
    @Transactional
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        // событие должно быть опубликовано
        Event event = eventRepository.findByIdAndState(eventId, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        UserShortDto userShortDto;
        try {
            userShortDto = userClient.getUserShort(event.getInitiatorId());
        } catch (FeignException e) {
            throw new NotFoundException("Not confirmed the existence of User " + event.getInitiatorId());
        }

        // информация о событии должна включать в себя количество просмотров и количество подтвержденных запросов
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
        Long views = viewRepository.countByEventId(eventId);

        // делаем новый уникальный просмотр
        if (!viewRepository.existsByEventIdAndIp(eventId, request.getRemoteAddr())) {
            View view = View.builder()
                    .event(event)
                    .ip(request.getRemoteAddr())
                    .build();
            viewRepository.save(view);
        }

        // информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики
        statClient.hit(EventHitDto.builder()
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .app("ewm-main-service")
                .timestamp(LocalDateTime.now())
                .build());

        return EventMapper.toEventFullDto(event, userShortDto, confirmedRequests, views);
    }

}