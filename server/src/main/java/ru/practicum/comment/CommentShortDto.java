package ru.practicum.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.user.UserDto;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentShortDto {

    long id;
    String text;
    UserDto author;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    String createTime;

}