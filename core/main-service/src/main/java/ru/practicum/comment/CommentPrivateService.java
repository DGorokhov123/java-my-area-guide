package ru.practicum.comment;

public interface CommentPrivateService {

    CommentDto createComment(Long userId, Long eventId, CommentCreateDto commentDto);

    void deleteComment(Long userId, Long comId);

    CommentDto patchComment(Long userId, Long comId, CommentCreateDto commentCreateDto);
}