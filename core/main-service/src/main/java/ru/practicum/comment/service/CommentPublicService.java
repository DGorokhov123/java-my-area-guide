package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentShortDto;

import java.util.List;

public interface CommentPublicService {

    CommentDto getComment(Long comId);

    List<CommentShortDto> getCommentsByEvent(Long eventId, int from, int size);

    CommentDto getCommentByEventAndCommentId(Long eventId, Long commentId);
}