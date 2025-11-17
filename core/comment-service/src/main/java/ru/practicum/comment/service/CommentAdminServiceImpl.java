package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.practicum.client.EventClientHelper;
import ru.practicum.client.UserClientHelper;
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

    private final TransactionTemplate transactionTemplate;
    private final CommentRepository commentRepository;

    private final UserClientHelper userClientHelper;
    private final EventClientHelper eventClientHelper;

    @Override
    @Transactional
    public String delete(Long comId) {
        if (!commentRepository.existsById(comId)) throw new NotFoundException("Not found Comment " + comId);
        commentRepository.deleteById(comId);
        return "deleted comment " + comId;
    }

    @Override
    public List<CommentDto> search(String text, int from, int size) {
        List<Comment> comments = transactionTemplate.execute(status -> {
            Pageable pageable = PageRequest.of(from / size, size);
            return commentRepository.findByText(text, pageable).getContent();
        });
        if (comments == null || comments.isEmpty()) return List.of();

        Set<Long> userIds = comments.stream().map(Comment::getAuthorId).collect(Collectors.toSet());
        Map<Long, UserDto> userMap = userClientHelper.retrieveUserDtoMapByUserIdList(userIds);

        Set<Long> eventIds = comments.stream().map(Comment::getEventId).collect(Collectors.toSet());
        Map<Long, EventCommentDto> eventMap = eventClientHelper.retrieveEventCommentDtoMapByUserIdList(eventIds);

        return comments.stream()
                .map(c -> CommentMapper.toCommentDto(
                        c,
                        userMap.get(c.getAuthorId()),
                        eventMap.get(c.getEventId())
                ))
                .toList();
    }

    @Override
    public List<CommentDto> findAllByUserId(Long userId, int from, int size) {
        UserDto userDto = userClientHelper.retrieveUserDtoByUserId(userId);

        List<Comment> comments = transactionTemplate.execute(status -> {
            Pageable pageable = PageRequest.of(from / size, size);
            return commentRepository.findAllByAuthorId(userId, pageable).getContent();
        });
        if (comments == null || comments.isEmpty()) return List.of();

        Set<Long> eventIds = comments.stream().map(Comment::getEventId).collect(Collectors.toSet());
        Map<Long, EventCommentDto> eventMap = eventClientHelper.retrieveEventCommentDtoMapByUserIdList(eventIds);

        return comments.stream()
                .map(c -> CommentMapper.toCommentDto(
                        c,
                        userDto,
                        eventMap.get(c.getEventId())
                ))
                .toList();
    }

    @Override
    public CommentDto approveComment(Long comId) {
        Comment comment = transactionTemplate.execute(status -> {
            Comment commentEntity = commentRepository.findById(comId)
                    .orElseThrow(() -> new NotFoundException("Not found Comment " + comId));
            commentEntity.setApproved(true);
            return commentRepository.save(commentEntity);
        });

        UserDto userDto = userClientHelper.retrieveUserDtoByUserId(comment.getAuthorId());
        EventCommentDto eventCommentDto = eventClientHelper.retrieveEventCommentDtoByEventId(comment.getEventId());

        return CommentMapper.toCommentDto(comment, userDto, eventCommentDto);
    }

    @Override
    public CommentDto rejectComment(Long comId) {
        Comment comment = transactionTemplate.execute(status -> {
            Comment commentEntity = commentRepository.findById(comId)
                    .orElseThrow(() -> new NotFoundException("Not found Comment " + comId));
            commentEntity.setApproved(false);
            return commentRepository.save(commentEntity);
        });

        UserDto userDto = userClientHelper.retrieveUserDtoByUserId(comment.getAuthorId());
        EventCommentDto eventCommentDto = eventClientHelper.retrieveEventCommentDtoByEventId(comment.getEventId());

        return CommentMapper.toCommentDto(comment, userDto, eventCommentDto);
    }

}