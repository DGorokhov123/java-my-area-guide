package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.user.UserShortDto;

import java.time.LocalDateTime;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class EventShortDto {

    private Long id;

    private UserShortDto initiator;
    private CategoryDto category;

    private String title;
    private String annotation;

    private Boolean paid;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private Long confirmedRequests;
    private Long views;

}
