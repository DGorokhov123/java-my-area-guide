package ru.practicum.request.mapper;

import ru.practicum.event.model.Event;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.Request;
import ru.practicum.user.model.User;

public class RequestMapper {

    public static ParticipationRequestDto toDto(Request request) {
        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(request.getId());
        dto.setRequester(request.getRequester().getId());
        dto.setEvent(request.getEvent().getId());
        dto.setStatus(request.getStatus());
        dto.setCreated(request.getCreated());
        return dto;
    }

    public static Request toEntity(ParticipationRequestDto dto, User requester, Event event) {
        Request request = new Request();
        request.setId(dto.getId());
        request.setRequester(requester);
        request.setEvent(event);
        request.setStatus(dto.getStatus());
        request.setCreated(dto.getCreated());
        return request;
    }

}
