package ru.practicum.dto.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.event.EventCommentDto;
import ru.practicum.dto.user.UserDto;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {

    private Long id;

    private String text;

    private UserDto author;

    private EventCommentDto event;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime patchTime;

    private Boolean approved;

}