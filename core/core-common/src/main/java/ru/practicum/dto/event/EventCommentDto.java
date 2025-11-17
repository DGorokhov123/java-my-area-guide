package ru.practicum.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventCommentDto {

    private Long id;

    private String title;

    private State state;

    public static EventCommentDto makeDummy(Long id) {
        EventCommentDto dto = new EventCommentDto();
        dto.setId(id);
        return dto;
    }

}
