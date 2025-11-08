package ru.practicum.dto.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.user.UserDto;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentShortDto {

    private Long id;

    private String text;

    private UserDto author;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String createTime;

}