package ru.practicum.api.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;

import java.util.Collection;

public interface CommentAdminApi {

    @GetMapping("/admin/comments/search")
    @ResponseStatus(HttpStatus.OK)
    Collection<CommentDto> search(
            @RequestParam @NotBlank String text,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    );

    @GetMapping("/admin/users/{userId}/comments")
    @ResponseStatus(HttpStatus.OK)
    Collection<CommentDto> get(
            @PathVariable @Positive Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    );

    @DeleteMapping("/admin/comments/{comId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    String delete(
            @PathVariable @Positive Long comId
    );

    @PatchMapping("/admin/comments/{comId}/approve")
    @ResponseStatus(HttpStatus.OK)
    CommentDto approveComment(
            @PathVariable @Positive Long comId
    );

    @PatchMapping("/admin/comments/{comId}/reject")
    @ResponseStatus(HttpStatus.OK)
    CommentDto rejectComment(
            @PathVariable @Positive Long comId
    );

}