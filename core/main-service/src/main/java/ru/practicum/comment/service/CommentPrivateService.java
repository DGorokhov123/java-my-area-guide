package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentCreateDto;
import ru.practicum.comment.dto.CommentDto;

public interface CommentPrivateService {

    CommentDto createComment(Long userId, Long eventId, CommentCreateDto commentDto);

    void deleteComment(Long userId, Long comId);

    CommentDto patchComment(Long userId, Long comId, CommentCreateDto commentCreateDto);
}