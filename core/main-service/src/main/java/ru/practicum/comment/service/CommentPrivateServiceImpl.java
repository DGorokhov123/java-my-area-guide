package ru.practicum.comment.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.UserClient;
import ru.practicum.comment.dal.Comment;
import ru.practicum.comment.dal.CommentRepository;
import ru.practicum.dto.comment.CommentCreateDto;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.event.State;
import ru.practicum.dto.user.UserDto;
import ru.practicum.event.dal.Event;
import ru.practicum.event.dal.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentPrivateServiceImpl implements CommentPrivateService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;

    private final UserClient userClient;

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long eventId, CommentCreateDto commentCreateDto) {
        UserDto userDto;
        try {
            userDto = userClient.getUser(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Not confirmed the existence of User " + userId);
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Not found Event " + eventId));

        if (!Objects.equals(event.getState(), State.PUBLISHED))
            throw new ConflictException("Unable to comment unpublished Event " + eventId);

        Comment comment = Comment.builder()
                .text(commentCreateDto.getText())
                .authorId(userId)
                .event(event)
                .approved(true)                                  // по умолчанию комменты видны
                .createTime(LocalDateTime.now())
                .build();
        commentRepository.save(comment);
        return CommentMapper.toCommentDto(comment, userDto);
    }

    @Override
    @Transactional
    public String deleteComment(Long userId, Long comId) {
        Comment comment = commentRepository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Not found Comment " + comId));
        if (!Objects.equals(comment.getAuthorId(), userId))
            throw new ConflictException("Unauthorized access by user " + userId + " to comment " + comId);
        commentRepository.deleteById(comId);
        return "Deleted Comment " + comId;
    }

    @Override
    @Transactional
    public CommentDto patchComment(Long userId, Long comId, CommentCreateDto commentCreateDto) {
        UserDto userDto;
        try {
            userDto = userClient.getUser(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Not confirmed the existence of User " + userId);
        }

        Comment comment = commentRepository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Not found Comment " + comId));

        if (!Objects.equals(comment.getAuthorId(), userId))
            throw new ConflictException("Unauthorized access by user " + userId + " to comment " + comId);

        comment.setText(commentCreateDto.getText());
        comment.setPatchTime(LocalDateTime.now());
        commentRepository.save(comment);
        return CommentMapper.toCommentDto(comment, userDto);
    }

}