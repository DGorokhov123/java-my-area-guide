package ru.practicum.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/admin")
@RequiredArgsConstructor
@Slf4j
public class CommentAdminController {

    private final CommentAdminService service;

    @GetMapping("/comments/search")
    public ResponseEntity<List<CommentDto>> search(@RequestParam @NotBlank String text,
                                                   @RequestParam(defaultValue = "0") int from,
                                                   @RequestParam(defaultValue = "10") int size) {
        log.info("Calling the GET request to /admin/comment/search endpoint");
        return ResponseEntity.ok(service.search(text, from, size));
    }

    @GetMapping("users/{userId}/comments")
    public ResponseEntity<List<CommentDto>> get(@PathVariable @Positive Long userId,
                                                @RequestParam(defaultValue = "0") int from,
                                                @RequestParam(defaultValue = "10") int size) {
        log.info("Calling the GET request to admin/users/{userId}/comment endpoint");
        return ResponseEntity.ok(service.findAllByUserId(userId, from, size));
    }

    @DeleteMapping("comments/{comId}")
    public ResponseEntity<String> delete(@PathVariable @Positive Long comId) {
        log.info("Calling the GET request to admin/comment/{comId} endpoint");
        service.delete(comId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/comments/{comId}/approve")
    public ResponseEntity<CommentDto> approveComment(@PathVariable @Positive Long comId) {
        log.info("Calling the PATCH request to /admin/comment/{comId}/approve endpoint");
        CommentDto commentDto = service.approveComment(comId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(commentDto);
    }

    @PatchMapping("/comments/{comId}/reject")
    public ResponseEntity<CommentDto> rejectComment(@PathVariable @Positive Long comId) {
        log.info("Calling the PATCH request to /admin/comment/{comId}/reject endpoint");
        CommentDto commentDto = service.rejectComment(comId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(commentDto);
    }
}