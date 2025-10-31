package ru.practicum.comment;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CommentPublicController {

    private final CommentPublicService service;

    @GetMapping("/comments/{comId}")
    public ResponseEntity<CommentDto> getById(@PathVariable @Positive Long comId) {
        log.info("Calling the GET request to /comments/{comId} endpoint");
        return ResponseEntity.ok(service.getComment(comId));
    }

    @GetMapping("/events/{eventId}/comments")
    public ResponseEntity<List<CommentShortDto>> getByEventId(@PathVariable @Positive Long eventId,
                                                              @RequestParam(defaultValue = "0") int from,
                                                              @RequestParam(defaultValue = "10") int size) {
        log.info("Calling the GET request to /events/{eventId}/comments");
        return ResponseEntity.ok(service.getCommentsByEvent(eventId, from, size));
    }

    @GetMapping("/events/{eventId}/comments/{commentId}")
    public ResponseEntity<CommentDto> getByEventAndCommentId(@PathVariable @Positive Long eventId,
                                                             @PathVariable @Positive Long commentId) {
        log.info("Calling the GET request to /events/{eventId}/comments/{commentId}");
        return ResponseEntity.ok(service.getCommentByEventAndCommentId(eventId, commentId));
    }

}