package ru.practicum.comment.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.comment.dal.Comment;
import ru.practicum.comment.dal.CommentRepository;
import ru.practicum.dto.comment.CommentCreateDto;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.event.EventCommentDto;
import ru.practicum.dto.event.State;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentPrivateServiceImpl implements CommentPrivateService {

    private final CommentRepository commentRepository;

    private final UserClient userClient;
    private final EventClient eventClient;

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long eventId, CommentCreateDto commentCreateDto) {
        log.trace("creating comment for user {} event {} dto {}", userId, eventId, commentCreateDto);
        UserDto userDto;
        try {
            userDto = userClient.getUser(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Not confirmed the existence of User " + userId);
        }

        EventCommentDto eventCommentDto;
        try {
            eventCommentDto = eventClient.getEventCommentDto(eventId);
        } catch (FeignException e) {
            throw new NotFoundException("Not confirmed the existence of Event " + eventId);
        }

        if (!Objects.equals(eventCommentDto.getState(), State.PUBLISHED))
            throw new ConflictException("Unable to comment unpublished Event " + eventId);

        Comment comment = Comment.builder()
                .text(commentCreateDto.getText())
                .authorId(userId)
                .eventId(eventId)
                .approved(true)                                  // по умолчанию комменты видны
                .createTime(LocalDateTime.now())
                .build();
        commentRepository.save(comment);
        return CommentMapper.toCommentDto(comment, userDto, eventCommentDto);
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
        Comment comment = commentRepository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Not found Comment " + comId));

        if (!Objects.equals(comment.getAuthorId(), userId))
            throw new ConflictException("Unauthorized access by user " + userId + " to comment " + comId);

        UserDto userDto;
        try {
            userDto = userClient.getUser(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Not confirmed the existence of User " + userId);
        }

        EventCommentDto eventCommentDto;
        try {
            eventCommentDto = eventClient.getEventCommentDto(comment.getEventId());
        } catch (FeignException e) {
            throw new NotFoundException("Not confirmed the existence of Event " + comment.getEventId());
        }

        comment.setText(commentCreateDto.getText());
        comment.setPatchTime(LocalDateTime.now());
        commentRepository.save(comment);
        return CommentMapper.toCommentDto(comment, userDto, eventCommentDto);
    }

}