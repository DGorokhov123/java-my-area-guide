package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.comment.CommentPublicApi;
import ru.practicum.comment.service.CommentPublicService;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.CommentShortDto;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@Validated
public class CommentPublicController implements CommentPublicApi {

    private final CommentPublicService commentPublicService;

    @Override
    public CommentDto getById(Long comId) {
        return commentPublicService.getComment(comId);
    }

    @Override
    public Collection<CommentShortDto> getByEventId(Long eventId, int from, int size) {
        return commentPublicService.getCommentsByEvent(eventId, from, size);
    }

    @Override
    public CommentDto getByEventAndCommentId(Long eventId, Long commentId) {
        return commentPublicService.getCommentByEventAndCommentId(eventId, commentId);
    }

}