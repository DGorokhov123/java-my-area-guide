package ru.practicum.comment.service;

import ru.practicum.comment.dal.Comment;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.CommentShortDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.event.service.EventMapper;

public class CommentMapper {

    public static CommentDto toCommentDto(Comment comment, UserDto author) {
        return CommentDto.builder()
                .id(comment.getId())
                .author(author)
                .event(EventMapper.toEventComment(comment.getEvent()))
                .createTime(comment.getCreateTime())
                .text(comment.getText())
                .approved(comment.getApproved())
                .build();
    }

    public static CommentShortDto toCommentShortDto(Comment comment, UserDto author) {
        return CommentShortDto.builder()
                .author(author)
                .createTime(comment.getText())
                .id(comment.getId())
                .text(comment.getText())
                .build();
    }

}