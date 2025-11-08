package ru.practicum.comment.service;

import ru.practicum.dto.comment.CommentCreateDto;
import ru.practicum.dto.comment.CommentDto;

public interface CommentPrivateService {

    CommentDto createComment(Long userId, Long eventId, CommentCreateDto commentDto);

    String deleteComment(Long userId, Long comId);

    CommentDto patchComment(Long userId, Long comId, CommentCreateDto commentCreateDto);

}