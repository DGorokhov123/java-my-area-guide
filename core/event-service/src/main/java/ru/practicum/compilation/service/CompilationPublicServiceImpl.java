package ru.practicum.compilation.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.UserClient;
import ru.practicum.compilation.dal.Compilation;
import ru.practicum.compilation.dal.CompilationRepository;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.event.dal.Event;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationPublicServiceImpl implements CompilationPublicService {

    private final CompilationRepository compilationRepository;

    private final UserClient userClient;

    @Override
    @Transactional(readOnly = true)
    public CompilationDto readCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found"));

        Set<Long> userIds = compilation.getEvents().stream().map(Event::getInitiatorId).collect(Collectors.toSet());
        Map<Long, UserShortDto> userMap;
        try {
            userMap = userClient.getUserShortDtoListByIds(userIds).stream()
                    .collect(Collectors.toMap(UserShortDto::getId, u -> u));
        } catch (FeignException e) {
            throw new NotFoundException("Unable to get info for Users in list " + userIds);
        }

        return CompilationMapper.toCompilationDto(compilation, userMap);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> readAllCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.Direction.ASC, "id");
        List<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(pageable).getContent();
        } else {
            compilations = compilationRepository.findAllByPinned(pinned, pageable).getContent();
        }

        Set<Long> userIds = compilations.stream()
                .flatMap(c -> c.getEvents().stream())
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());
        Map<Long, UserShortDto> userMap;
        try {
            userMap = userClient.getUserShortDtoListByIds(userIds).stream()
                    .collect(Collectors.toMap(UserShortDto::getId, u -> u));
        } catch (FeignException e) {
            throw new NotFoundException("Unable to get info for Users in list " + userIds);
        }

        return compilations.stream()
                .map(c -> CompilationMapper.toCompilationDto(c, userMap))
                .toList();
    }

}