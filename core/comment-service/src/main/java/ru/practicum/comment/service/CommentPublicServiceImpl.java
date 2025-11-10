package ru.practicum.comment.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
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

    private final CommentRepository commentRepository;

    private final UserClient userClient;
    private final EventClient eventClient;

    @Override
    @Transactional(readOnly = true)
    public CommentDto getComment(Long comId) {
        Comment comment = commentRepository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Not found Comment " + comId));

        if (!Objects.equals(comment.getApproved(), true))
            throw new ForbiddenException("Comment " + comId + "is not approved");

        UserDto userDto;
        try {
            userDto = userClient.getUser(comment.getAuthorId());
        } catch (FeignException e) {
            throw new NotFoundException("Not confirmed the existence of User " + comment.getAuthorId());
        }

        EventCommentDto eventCommentDto;
        try {
            eventCommentDto = eventClient.getEventCommentDto(comment.getEventId());
        } catch (FeignException e) {
            throw new NotFoundException("Not confirmed the existence of Event " + comment.getEventId());
        }

        return CommentMapper.toCommentDto(comment, userDto, eventCommentDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentShortDto> getCommentsByEvent(Long eventId, int from, int size) {
        EventCommentDto eventCommentDto;
        try {
            eventCommentDto = eventClient.getEventCommentDto(eventId);
        } catch (FeignException e) {
            throw new NotFoundException("Not confirmed the existence of Event " + eventId);
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("createTime").ascending());
        Page<Comment> comments = commentRepository.findAllByEventIdAndApproved(eventId, true, pageable);

        Set<Long> userIds = comments.stream().map(Comment::getAuthorId).collect(Collectors.toSet());
        Map<Long, UserDto> userMap;
        try {
            userMap = userClient.getUserDtoListByIds(userIds).stream()
                    .collect(Collectors.toMap(UserDto::getId, u -> u));
        } catch (FeignException e) {
            throw new NotFoundException("Unable to get info for Users in list " + userIds);
        }

        return comments.stream()
                .map(c -> CommentMapper.toCommentShortDto(c, userMap.get(c.getAuthorId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getCommentByEventAndCommentId(Long eventId, Long comId) {
        Comment comment = commentRepository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Not found Comment " + comId));

        if (!Objects.equals(comment.getEventId(), eventId))
            throw new NotFoundException("Comment " + comId + " does not belong to Event " + eventId);

        if (!Objects.equals(comment.getApproved(), true))
            throw new ForbiddenException("Comment " + comId + "is not approved");

        UserDto userDto;
        try {
            userDto = userClient.getUser(comment.getAuthorId());
        } catch (FeignException e) {
            throw new NotFoundException("Not confirmed the existence of User " + comment.getAuthorId());
        }

        EventCommentDto eventCommentDto;
        try {
            eventCommentDto = eventClient.getEventCommentDto(comment.getEventId());
        } catch (FeignException e) {
            throw new NotFoundException("Not confirmed the existence of Event " + comment.getEventId());
        }

        return CommentMapper.toCommentDto(comment, userDto, eventCommentDto);
    }

}