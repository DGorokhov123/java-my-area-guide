package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.practicum.client.EventClientHelper;
import ru.practicum.client.UserClientHelper;
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

    private final TransactionTemplate transactionTemplate;
    private final CommentRepository commentRepository;

    private final UserClientHelper userClientHelper;
    private final EventClientHelper eventClientHelper;

    @Override
    public CommentDto createComment(Long userId, Long eventId, CommentCreateDto commentCreateDto) {
        UserDto userDto = userClientHelper.retrieveUserDtoByUserIdOrFall(userId);
        EventCommentDto eventCommentDto = eventClientHelper.retrieveEventCommentDtoByEventIdOrFall(eventId);

        if (!Objects.equals(eventCommentDto.getState(), State.PUBLISHED))
            throw new ConflictException("Unable to comment unpublished Event " + eventId);

        return transactionTemplate.execute(status -> {
            Comment comment = Comment.builder()
                    .text(commentCreateDto.getText())
                    .authorId(userId)
                    .eventId(eventId)
                    .approved(true)                                  // по умолчанию комменты видны
                    .createTime(LocalDateTime.now())
                    .build();
            commentRepository.save(comment);
            return CommentMapper.toCommentDto(comment, userDto, eventCommentDto);
        });
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
    public CommentDto patchComment(Long userId, Long comId, CommentCreateDto commentCreateDto) {
        Comment comment = transactionTemplate.execute(status -> {
            Comment commentEntity = commentRepository.findById(comId)
                    .orElseThrow(() -> new NotFoundException("Not found Comment " + comId));

            if (!Objects.equals(commentEntity.getAuthorId(), userId))
                throw new ConflictException("Unauthorized access by user " + userId + " to comment " + comId);

            commentEntity.setText(commentCreateDto.getText());
            commentEntity.setPatchTime(LocalDateTime.now());
            return commentRepository.save(commentEntity);
        });

        UserDto userDto = userClientHelper.retrieveUserDtoByUserIdOrFall(userId);
        EventCommentDto eventCommentDto = eventClientHelper.retrieveEventCommentDtoByEventIdOrFall(comment.getEventId());

        return CommentMapper.toCommentDto(comment, userDto, eventCommentDto);
    }

}