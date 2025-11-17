package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import ru.practicum.client.UserClientHelper;
import ru.practicum.compilation.dal.Compilation;
import ru.practicum.compilation.dal.CompilationRepository;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.event.dal.Event;
import ru.practicum.exception.NotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationPublicServiceImpl implements CompilationPublicService {

    private final TransactionTemplate transactionTemplate;
    private final CompilationRepository compilationRepository;

    private final UserClientHelper userClientHelper;

    @Override
    public CompilationDto readCompilationById(Long compId) {
        Compilation compilation = transactionTemplate.execute(status -> {
            return compilationRepository.findById(compId)
                    .orElseThrow(() -> new NotFoundException("Compilation not found"));
        });

        Map<Long, UserShortDto> userMap = new HashMap<>();
        if (compilation.getEvents() != null && !compilation.getEvents().isEmpty()) {
            Set<Long> userIds = compilation.getEvents().stream().map(Event::getInitiatorId).collect(Collectors.toSet());
            userMap = userClientHelper.retrieveUserShortDtoMapByUserIdList(userIds);
        }

        return CompilationMapper.toCompilationDto(compilation, userMap);
    }

    @Override
    public List<CompilationDto> readAllCompilations(Boolean pinned, int from, int size) {
        List<Compilation> compilations = transactionTemplate.execute(status -> {
            Pageable pageable = PageRequest.of(from / size, size, Sort.Direction.ASC, "id");
            if (pinned == null) {
                return compilationRepository.findAll(pageable).getContent();
            } else {
                return compilationRepository.findAllByPinned(pinned, pageable).getContent();
            }
        });
        if (compilations == null || compilations.isEmpty()) return List.of();

        Set<Long> userIds = compilations.stream()
                .flatMap(c -> c.getEvents().stream())
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());
        Map<Long, UserShortDto> userMap = userClientHelper.retrieveUserShortDtoMapByUserIdList(userIds);

        return compilations.stream()
                .map(c -> CompilationMapper.toCompilationDto(c, userMap))
                .toList();
    }

}