package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.request.RequestApi;
import ru.practicum.dto.request.EventRequestStatusUpdateRequestDto;
import ru.practicum.dto.request.EventRequestStatusUpdateResultDto;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.Collection;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Validated
public class RequestController implements RequestApi {

    private final RequestService requestService;

    // ЗАЯВКИ ТЕКУЩЕГО ПОЛЬЗОВАТЕЛЯ

    // Добавление запроса от текущего пользователя на участие в событии
    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        return requestService.addRequest(userId, eventId);
    }

    // Отмена своего запроса на участие в событии
    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }

    // Получение информации о заявках текущего пользователя на участие в чужих событиях
    @Override
    public Collection<ParticipationRequestDto> getRequesterRequests(Long userId) {
        return requestService.findRequesterRequests(userId);
    }

    // ЗАЯВКИ НА КОНКРЕТНОЕ СОБЫТИЕ

    // Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя
    @Override
    public EventRequestStatusUpdateResultDto moderateRequest(Long userId, Long eventId,
                                                             EventRequestStatusUpdateRequestDto updateRequestDto) {
        return requestService.moderateRequest(userId, eventId, updateRequestDto);
    }

    // Получение информации о запросах на участие в событии текущего пользователя
    @Override
    public Collection<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        return requestService.findEventRequests(userId, eventId);
    }

    @Override
    public Map<Long, Long> getConfirmedRequestsByEventIds(Collection<Long> eventIds) {
        return requestService.getConfirmedRequestsByEventIds(eventIds);
    }

    @Override
    public String checkParticipation(Long userId, Long eventId) {
        return requestService.checkParticipation(userId, eventId);
    }

}