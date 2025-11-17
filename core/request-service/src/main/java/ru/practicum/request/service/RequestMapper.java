package ru.practicum.request.service;

import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.request.dal.Request;

public class RequestMapper {

    public static ParticipationRequestDto toDto(Request request) {
        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(request.getId());
        dto.setRequester(request.getRequesterId());
        dto.setEvent(request.getEventId());
        dto.setStatus(request.getStatus());
        dto.setCreated(request.getCreated());
        return dto;
    }

}
