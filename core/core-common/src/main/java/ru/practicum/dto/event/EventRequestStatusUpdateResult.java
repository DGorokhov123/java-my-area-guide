package ru.practicum.dto.event;

import lombok.Data;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;

@Data
public class EventRequestStatusUpdateResult {

    private List<ParticipationRequestDto> confirmedRequests;

    private List<ParticipationRequestDto> rejectedRequests;

}
