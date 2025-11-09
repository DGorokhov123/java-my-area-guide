package ru.practicum.comment.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.UserClient;
import ru.practicum.comment.dal.Comment;
import ru.practicum.comment.dal.CommentRepository;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.CommentShortDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.event.dal.EventRepository;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.practicum.util.Util.createPageRequestAsc;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentPublicServiceImpl implements CommentPublicService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;

    private final UserClient userClient;

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

        return CommentMapper.toCommentDto(comment, userDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentShortDto> getCommentsByEvent(Long eventId, int from, int size) {
        if (!eventRepository.existsById(eventId)) throw new NotFoundException("Not found Event " + eventId);

        Pageable pageable = createPageRequestAsc("createTime", from / size, size);
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

        if (!Objects.equals(comment.getEvent().getId(), eventId))
            throw new NotFoundException("Comment " + comId + " does not belong to Event " + eventId);

        if (!Objects.equals(comment.getApproved(), true))
            throw new ForbiddenException("Comment " + comId + "is not approved");

        UserDto userDto;
        try {
            userDto = userClient.getUser(comment.getAuthorId());
        } catch (FeignException e) {
            throw new NotFoundException("Not confirmed the existence of User " + comment.getAuthorId());
        }

        return CommentMapper.toCommentDto(comment, userDto);
    }

}