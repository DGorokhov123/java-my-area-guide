package ru.practicum.event.dto;

import lombok.Data;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {

    private List<Integer> requestIds;

    private State state;

}
