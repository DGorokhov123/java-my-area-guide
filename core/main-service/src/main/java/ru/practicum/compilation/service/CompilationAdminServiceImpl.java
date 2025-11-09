package ru.practicum.compilation.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.UserClient;
import ru.practicum.compilation.dal.Compilation;
import ru.practicum.compilation.dal.CompilationRepository;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.event.dal.Event;
import ru.practicum.event.dal.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationAdminServiceImpl implements CompilationAdminService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    private final UserClient userClient;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        Set<Event> events = new HashSet<>();
        Map<Long, UserShortDto> userMap = new HashMap<>();

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            events = new HashSet<>(eventRepository.findAllById(newCompilationDto.getEvents()));
            Set<Long> userIds = events.stream().map(Event::getInitiatorId).collect(Collectors.toSet());
            try {
                userMap = userClient.getUserShortDtoListByIds(userIds).stream()
                        .collect(Collectors.toMap(UserShortDto::getId, u -> u));
            } catch (FeignException e) {
                throw new NotFoundException("Unable to get info for Users in list " + userIds);
            }
        }

        Compilation compilation = Compilation.builder()
                .pinned(newCompilationDto.getPinned() != null && newCompilationDto.getPinned())
                .title(newCompilationDto.getTitle())
                .events(events)
                .build();
        compilationRepository.save(compilation);
        return CompilationMapper.toCompilationDto(compilation, userMap);
    }

    @Override
    @Transactional
    public String deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) throw new NotFoundException("Not found Compilation " + compId);
        compilationRepository.deleteById(compId);
        return "Compilation deleted: " + compId;
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationDto updateCompilationDto) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Not found Compilation " + compId));

        Set<Long> userIds = compilation.getEvents().stream().map(Event::getInitiatorId).collect(Collectors.toSet());
        Map<Long, UserShortDto> userMap;
        try {
            userMap = userClient.getUserShortDtoListByIds(userIds).stream()
                    .collect(Collectors.toMap(UserShortDto::getId, u -> u));
        } catch (FeignException e) {
            throw new NotFoundException("Unable to get info for Users in list " + userIds);
        }

        if (updateCompilationDto.getTitle() != null) {
            compilation.setTitle(updateCompilationDto.getTitle());
        }
        if (updateCompilationDto.getPinned() != null) {
            compilation.setPinned(updateCompilationDto.getPinned());
        }
        if (updateCompilationDto.getEvents() != null && !updateCompilationDto.getEvents().isEmpty()) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(updateCompilationDto.getEvents()));
            compilation.setEvents(events);
        }
        compilationRepository.save(compilation);
        return CompilationMapper.toCompilationDto(compilation, userMap);
    }

}