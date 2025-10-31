package ru.practicum.comment;

import lombok.experimental.UtilityClass;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.user.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class CommentMapper {

    public Comment toComment(CommentCreateDto commentDto) {
        return Comment.builder()
                .text(commentDto.getText())
                .build();
    }

    public CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .author(UserMapper.toDto(comment.getAuthor()))
                .event(EventMapper.toEventComment(comment.getEvent()))
                .createTime(comment.getCreateTime())
                .text(comment.getText())
                .approved(comment.getApproved())
                .build();
    }

    public List<CommentDto> toListCommentDto(List<Comment> list) {
        return list.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList());
    }

    public CommentShortDto toCommentShortDto(Comment comment) {
        return CommentShortDto.builder()
                .author(UserMapper.toDto(comment.getAuthor()))
                .createTime(comment.getText())
                .id(comment.getId())
                .text(comment.getText())
                .build();
    }

    public List<CommentShortDto> toListCommentShortDto(List<Comment> list) {
        return list.stream().map(CommentMapper::toCommentShortDto).collect(Collectors.toList());
    }
}