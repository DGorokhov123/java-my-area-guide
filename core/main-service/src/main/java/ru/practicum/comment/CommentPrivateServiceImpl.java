package ru.practicum.comment;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.dto.State;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class CommentPrivateServiceImpl implements CommentPrivateService {

    CommentRepository repository;
    UserRepository userRepository;
    EventRepository eventRepository;

    @Override
    public CommentDto createComment(Long userId, Long eventId, CommentCreateDto commentDto) {
        log.info("createComment - invoked");
        Comment comment = CommentMapper.toComment(commentDto);
        User author = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User with id = {} - not registered", userId);
                    return new NotFoundException("Please register first then you can comment");
                });
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Event with id = {} - not exist", eventId);
                    return new NotFoundException("Event not found");
                });
        if (!event.getState().equals(State.PUBLISHED)) {
            log.error("Event state = {} - should be PUBLISHED", event.getState());
            throw new ConflictException("Event not published you cant comment it");
        }
        comment.setAuthor(author);
        comment.setEvent(event);
        comment.setApproved(true);   // по умолчанию комменты видны, но админ может удалить/вернуть
        comment.setCreateTime(LocalDateTime.now().withNano(0));
        log.info("Result: new comment created");
        return CommentMapper.toCommentDto(repository.save(comment));
    }

    @Override
    public void deleteComment(Long userId, Long comId) {
        log.info("deleteComment - invoked");
        Comment comment = repository.findById(comId)
                .orElseThrow(() -> {
                    log.error("Comment with id = {} - not exist", comId);
                    return new NotFoundException("Comment not found");
                });
        if (!comment.getAuthor().getId().equals(userId)) {
            log.error("Unauthorized access by user");
            throw new ConflictException("you didn't write this comment and can't delete it");
        }
        log.info("Result: comment with id = {} - deleted", comId);
        repository.deleteById(comId);
    }

    @Override
    public CommentDto patchComment(Long userId, Long comId, CommentCreateDto commentCreateDto) {
        log.info("patchComment - invoked");
        Comment comment = repository.findById(comId)
                .orElseThrow(() -> {
                    log.error("Comment with id = {} - not exist", comId);
                    return new NotFoundException("Comment not found");
                });
        if (!comment.getAuthor().getId().equals(userId)) {
            log.error("Unauthorized access by user");
            throw new ConflictException("you didn't write this comment and can't patch it");
        }
        comment.setText(commentCreateDto.getText());
        comment.setPatchTime(LocalDateTime.now().withNano(0));
        log.info("Result: comment with id = {} - updated", comId);
        return CommentMapper.toCommentDto(comment);
    }
}