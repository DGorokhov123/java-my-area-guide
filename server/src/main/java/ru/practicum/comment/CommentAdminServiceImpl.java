package ru.practicum.comment;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class CommentAdminServiceImpl implements CommentAdminService {

    CommentRepository repository;
    UserRepository userRepository;

    @Override
    public void delete(Long comId) {
        log.info("admin delete - invoked");
        if (!repository.existsById(comId)) {
            log.error("User with id = {} not exist", comId);
            throw new NotFoundException("Comment not found");
        }
        log.info("Result: comment with id = {} deleted", comId);
        repository.deleteById(comId);
    }

    @Override
    public List<CommentDto> search(String text, int from, int size) {
        log.info("admin search - invoked");
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Comment> page = repository.findAllByText(text, pageable);
        List<Comment> list = page.getContent();
        log.info("Result: list of comments size = {} ", list.size());
        return CommentMapper.toListCommentDto(list);
    }

    @Override
    public List<CommentDto> findAllByUserId(Long userId, int from, int size) {
        log.info("admin findAllByUserId - invoked");
        if (!userRepository.existsById(userId)) {
            log.error("User with id = {} not exist", userId);
            throw new NotFoundException("User not found");
        }
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Comment> page = repository.findAllByAuthorId(userId, pageable);
        List<Comment> list = page.getContent();
        log.info("Result: list of comments size = {} ", list.size());
        return CommentMapper.toListCommentDto(list);
    }

    @Override
    public CommentDto approveComment(Long comId) {
        log.info("approveComment - invoked");
        Comment comment = repository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));
        comment.setApproved(true);
        repository.save(comment);
        log.info("Result: comment with id = {} approved", comId);
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    public CommentDto rejectComment(Long comId) {
        log.info("rejectComment - invoked");
        Comment comment = repository.findById(comId).orElseThrow(() -> new NotFoundException("Comment not found"));
        comment.setApproved(false);
        repository.save(comment);
        log.info("Result: comment with id = {} rejected", comId);
        return CommentMapper.toCommentDto(comment);
    }
}