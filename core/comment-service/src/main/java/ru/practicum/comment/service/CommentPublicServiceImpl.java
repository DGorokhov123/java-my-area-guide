package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import ru.practicum.client.EventClientHelper;
import ru.practicum.client.UserClientHelper;
import ru.practicum.comment.dal.Comment;
import ru.practicum.comment.dal.CommentRepository;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.CommentShortDto;
import ru.practicum.dto.event.EventCommentDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentPublicServiceImpl implements CommentPublicService {

    private final TransactionTemplate transactionTemplate;
    private final CommentRepository commentRepository;

    private final UserClientHelper userClientHelper;
    private final EventClientHelper eventClientHelper;

    @Override
    public CommentDto getComment(Long comId) {
        Comment comment = transactionTemplate.execute(status -> {
            return commentRepository.findById(comId)
                    .orElseThrow(() -> new NotFoundException("Not found Comment " + comId));
        });

        if (!Objects.equals(comment.getApproved(), true))
            throw new ForbiddenException("Comment " + comId + "is not approved");

        UserDto userDto = userClientHelper.retrieveUserDtoByUserId(comment.getAuthorId());
        EventCommentDto eventCommentDto = eventClientHelper.retrieveEventCommentDtoByEventId(comment.getEventId());

        return CommentMapper.toCommentDto(comment, userDto, eventCommentDto);
    }

    @Override
    public List<CommentShortDto> getCommentsByEvent(Long eventId, int from, int size) {
        EventCommentDto eventCommentDto = eventClientHelper.retrieveEventCommentDtoByEventId(eventId);

        List<Comment> comments = transactionTemplate.execute(status -> {
            Pageable pageable = PageRequest.of(from / size, size, Sort.by("createTime").ascending());
            return commentRepository.findAllByEventIdAndApproved(eventId, true, pageable).getContent();
        });
        if (comments == null || comments.isEmpty()) return List.of();

        Set<Long> userIds = comments.stream().map(Comment::getAuthorId).collect(Collectors.toSet());
        Map<Long, UserDto> userMap = userClientHelper.retrieveUserDtoMapByUserIdList(userIds);

        return comments.stream()
                .map(c -> CommentMapper.toCommentShortDto(c, userMap.get(c.getAuthorId())))
                .toList();
    }

    @Override
    public CommentDto getCommentByEventAndCommentId(Long eventId, Long comId) {
        Comment comment = transactionTemplate.execute(status -> {
            return commentRepository.findById(comId)
                    .orElseThrow(() -> new NotFoundException("Not found Comment " + comId));
        });

        if (!Objects.equals(comment.getEventId(), eventId))
            throw new NotFoundException("Comment " + comId + " does not belong to Event " + eventId);

        if (!Objects.equals(comment.getApproved(), true))
            throw new ForbiddenException("Comment " + comId + "is not approved");

        UserDto userDto = userClientHelper.retrieveUserDtoByUserId(comment.getAuthorId());
        EventCommentDto eventCommentDto = eventClientHelper.retrieveEventCommentDtoByEventId(comment.getEventId());

        return CommentMapper.toCommentDto(comment, userDto, eventCommentDto);
    }

}