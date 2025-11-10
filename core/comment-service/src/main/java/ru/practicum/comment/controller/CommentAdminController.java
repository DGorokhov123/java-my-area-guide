package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.comment.CommentAdminApi;
import ru.practicum.comment.service.CommentAdminService;
import ru.practicum.dto.comment.CommentDto;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@Validated
public class CommentAdminController implements CommentAdminApi {

    private final CommentAdminService commentAdminService;

    @Override
    public Collection<CommentDto> search(String text, int from, int size) {
        return commentAdminService.search(text, from, size);
    }

    @Override
    public Collection<CommentDto> get(Long userId, int from, int size) {
        return commentAdminService.findAllByUserId(userId, from, size);
    }

    @Override
    public String delete(Long comId) {
        return commentAdminService.delete(comId);
    }

    @Override
    public CommentDto approveComment(Long comId) {
        return commentAdminService.approveComment(comId);
    }

    @Override
    public CommentDto rejectComment(Long comId) {
        return commentAdminService.rejectComment(comId);
    }

}