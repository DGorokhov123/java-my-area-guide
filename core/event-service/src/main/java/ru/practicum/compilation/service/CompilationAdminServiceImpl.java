package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.practicum.client.UserClientHelper;
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

    private final TransactionTemplate transactionTemplate;
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    private final UserClientHelper userClientHelper;

    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        Set<Event> events = new HashSet<>();
        Map<Long, UserShortDto> userMap = new HashMap<>();

        if (newCompilationDto.getPinned() == null) newCompilationDto.setPinned(false);

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            events = transactionTemplate.execute(status -> {
                return new HashSet<>(eventRepository.findAllById(newCompilationDto.getEvents()));
            });
            Set<Long> userIds = events.stream().map(Event::getInitiatorId).collect(Collectors.toSet());
            userMap = userClientHelper.retrieveUserShortDtoMapByUserIdList(userIds);
        }

        Set<Event> eventsFinal = events;
        Map<Long, UserShortDto> userMapFinal = userMap;

        return transactionTemplate.execute(status -> {
            Compilation compilation = Compilation.builder()
                    .pinned(newCompilationDto.getPinned())
                    .title(newCompilationDto.getTitle())
                    .events(eventsFinal)
                    .build();
            compilationRepository.save(compilation);
            return CompilationMapper.toCompilationDto(compilation, userMapFinal);
        });
    }

    @Override
    @Transactional
    public String deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) throw new NotFoundException("Not found Compilation " + compId);
        compilationRepository.deleteById(compId);
        return "Compilation deleted: " + compId;
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationDto updateCompilationDto) {
        Set<Long> userIds = transactionTemplate.execute(status -> {
            Compilation compilation = compilationRepository.findById(compId)
                    .orElseThrow(() -> new NotFoundException("Not found Compilation " + compId));
            return compilation.getEvents().stream().map(Event::getInitiatorId).collect(Collectors.toSet());
        });

        Map<Long, UserShortDto> userMap = userClientHelper.retrieveUserShortDtoMapByUserIdList(userIds);

        return transactionTemplate.execute(status -> {
            Compilation compilation = compilationRepository.findById(compId)
                    .orElseThrow(() -> new NotFoundException("Not found Compilation " + compId));

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
        });
    }

}