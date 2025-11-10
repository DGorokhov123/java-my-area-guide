package ru.practicum.comment.service;

import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.CommentShortDto;

import java.util.List;

public interface CommentPublicService {

    CommentDto getComment(Long comId);

    List<CommentShortDto> getCommentsByEvent(Long eventId, int from, int size);

    CommentDto getCommentByEventAndCommentId(Long eventId, Long commentId);

}