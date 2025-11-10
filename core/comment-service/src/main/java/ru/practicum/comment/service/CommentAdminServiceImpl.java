package ru.practicum.comment.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.comment.dal.Comment;
import ru.practicum.comment.dal.CommentRepository;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.event.EventCommentDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentAdminServiceImpl implements CommentAdminService {

    private final CommentRepository commentRepository;

    private final UserClient userClient;
    private final EventClient eventClient;

    @Override
    @Transactional
    public String delete(Long comId) {
        if (!commentRepository.existsById(comId)) throw new NotFoundException("Not found Comment " + comId);
        commentRepository.deleteById(comId);
        return "deleted comment " + comId;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> search(String text, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findByText(text, pageable).getContent();

        Set<Long> userIds = comments.stream().map(Comment::getAuthorId).collect(Collectors.toSet());
        Map<Long, UserDto> userMap;
        try {
            userMap = userClient.getUserDtoListByIds(userIds).stream()
                    .collect(Collectors.toMap(UserDto::getId, u -> u));
        } catch (FeignException e) {
            throw new NotFoundException("Unable to get info for Users in list " + userIds);
        }

        Set<Long> eventIds = comments.stream().map(Comment::getEventId).collect(Collectors.toSet());
        Map<Long, EventCommentDto> eventMap;
        try {
            eventMap = eventClient.getEventCommentDtoList(eventIds).stream()
                    .collect(Collectors.toMap(EventCommentDto::getId, e -> e));
        } catch (FeignException e) {
            throw new NotFoundException("Unable to get info for Events in list " + eventIds);
        }

        return comments.stream()
                .map(c -> CommentMapper.toCommentDto(
                        c,
                        userMap.get(c.getAuthorId()),
                        eventMap.get(c.getEventId())
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> findAllByUserId(Long userId, int from, int size) {
        UserDto userDto;
        try {
            userDto = userClient.getUser(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Not confirmed the existence of User " + userId);
        }
        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findAllByAuthorId(userId, pageable).getContent();

        Set<Long> eventIds = comments.stream().map(Comment::getEventId).collect(Collectors.toSet());
        Map<Long, EventCommentDto> eventMap;
        try {
            eventMap = eventClient.getEventCommentDtoList(eventIds).stream()
                    .collect(Collectors.toMap(EventCommentDto::getId, e -> e));
        } catch (FeignException e) {
            throw new NotFoundException("Unable to get info for Events in list " + eventIds);
        }

        return comments.stream()
                .map(c -> CommentMapper.toCommentDto(
                        c,
                        userDto,
                        eventMap.get(c.getEventId())
                ))
                .toList();
    }

    @Override
    @Transactional
    public CommentDto approveComment(Long comId) {
        Comment comment = commentRepository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Not found Comment " + comId));

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

        comment.setApproved(true);
        commentRepository.save(comment);
        return CommentMapper.toCommentDto(comment, userDto, eventCommentDto);
    }

    @Override
    @Transactional
    public CommentDto rejectComment(Long comId) {
        Comment comment = commentRepository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Not found Comment " + comId));

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

        comment.setApproved(false);
        commentRepository.save(comment);
        return CommentMapper.toCommentDto(comment, userDto, eventCommentDto);
    }

}