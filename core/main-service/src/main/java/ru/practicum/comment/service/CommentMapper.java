package ru.practicum.comment.service;

import ru.practicum.dto.comment.CommentCreateDto;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.CommentShortDto;
import ru.practicum.comment.dal.Comment;
import ru.practicum.event.service.EventMapper;
import ru.practicum.user.service.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

public class CommentMapper {

    public static Comment toComment(CommentCreateDto commentDto) {
        return Comment.builder()
                .text(commentDto.getText())
                .build();
    }

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .author(UserMapper.toDto(comment.getAuthor()))
                .event(EventMapper.toEventComment(comment.getEvent()))
                .createTime(comment.getCreateTime())
                .text(comment.getText())
                .approved(comment.getApproved())
                .build();
    }

    public static List<CommentDto> toListCommentDto(List<Comment> list) {
        return list.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList());
    }

    public static CommentShortDto toCommentShortDto(Comment comment) {
        return CommentShortDto.builder()
                .author(UserMapper.toDto(comment.getAuthor()))
                .createTime(comment.getText())
                .id(comment.getId())
                .text(comment.getText())
                .build();
    }

    public static List<CommentShortDto> toListCommentShortDto(List<Comment> list) {
        return list.stream().map(CommentMapper::toCommentShortDto).collect(Collectors.toList());
    }

}