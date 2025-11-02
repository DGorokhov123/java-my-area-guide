package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.category.CategoryDto;
import ru.practicum.comment.CommentShortDto;
import ru.practicum.user.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventFullDto {

    Long id;

    UserShortDto initiator;
    CategoryDto category;

    String title;
    String annotation;
    String description;

    State state;

    LocationDto location;

    Long participantLimit;
    Boolean requestModeration;
    Boolean paid;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime publishedOn;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdOn;

    Long confirmedRequests;
    Long views;

    List<CommentShortDto> comments;

}
