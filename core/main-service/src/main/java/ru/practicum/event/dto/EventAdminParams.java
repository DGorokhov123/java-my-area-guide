package ru.practicum.event.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class EventAdminParams {

    List<Long> users;

    List<State> states;

    List<Long> categories;

    LocalDateTime rangeStart;

    LocalDateTime rangeEnd;

    Long from;

    Long size;

}
