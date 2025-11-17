package ru.practicum.comment.service;

import ru.practicum.comment.dal.Comment;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.CommentShortDto;
import ru.practicum.dto.event.EventCommentDto;
import ru.practicum.dto.user.UserDto;

public class CommentMapper {

    public static CommentDto toCommentDto(Comment comment, UserDto author, EventCommentDto eventCommentDto) {
        return CommentDto.builder()
                .id(comment.getId())
                .author(author)
                .event(eventCommentDto)
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