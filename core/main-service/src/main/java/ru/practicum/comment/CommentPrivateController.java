package ru.practicum.comment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequiredArgsConstructor
@Slf4j
public class CommentPrivateController {

    private final CommentPrivateService service;

    @PostMapping("/users/{userId}/events/{eventId}/comments")
    public ResponseEntity<CommentDto> create(@PathVariable @Positive Long userId,
                                             @PathVariable @Positive Long eventId,
                                             @RequestBody @Valid CommentCreateDto commentCreateDto) {
        log.info("Calling the GET request to /users/{userId}/events/{eventId}/comment endpoint");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createComment(userId, eventId, commentCreateDto));
    }

    @DeleteMapping("/users/{userId}/comments/{comId}")
    public ResponseEntity<String> delete(@PathVariable @Positive Long userId,
                                         @PathVariable @Positive Long comId) {
        log.info("Calling the GET request to /users/{userId}/comment/{comId} endpoint");
        service.deleteComment(userId, comId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body("Comment deleted by user: " + comId);
    }

    @PatchMapping("/users/{userId}/comments/{comId}")
    public ResponseEntity<CommentDto> patch(@PathVariable @Positive Long userId,
                                            @PathVariable @Positive Long comId,
                                            @RequestBody @Valid CommentCreateDto commentCreateDto) {
        log.info("Calling the PATCH request to users/{userId}/comment/{comId} endpoint");
        return ResponseEntity.ok(service.patchComment(userId, comId, commentCreateDto));
    }
}