package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.comment.CommentPrivateApi;
import ru.practicum.comment.service.CommentPrivateService;
import ru.practicum.dto.comment.CommentCreateDto;
import ru.practicum.dto.comment.CommentDto;

@RestController
@Validated
@RequiredArgsConstructor
public class CommentPrivateController implements CommentPrivateApi {

    private final CommentPrivateService service;

    @Override
    public CommentDto create(Long userId, Long eventId, CommentCreateDto commentCreateDto) {
        return service.createComment(userId, eventId, commentCreateDto);
    }

    @Override
    public String delete(Long userId, Long comId) {
        return service.deleteComment(userId, comId);
    }

    @Override
    public CommentDto patch(Long userId, Long comId, CommentCreateDto commentCreateDto) {
        return service.patchComment(userId, comId, commentCreateDto);
    }

}