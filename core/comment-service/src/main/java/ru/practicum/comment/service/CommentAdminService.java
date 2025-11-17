package ru.practicum.comment.service;

import ru.practicum.dto.comment.CommentDto;

import java.util.List;

public interface CommentAdminService {

    String delete(Long comId);

    List<CommentDto> search(String text, int from, int size);

    List<CommentDto> findAllByUserId(Long userId, int from, int size);

    CommentDto approveComment(Long comId);

    CommentDto rejectComment(Long comId);

}