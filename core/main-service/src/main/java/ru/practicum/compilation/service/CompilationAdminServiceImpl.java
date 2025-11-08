package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dal.Compilation;
import ru.practicum.compilation.dal.CompilationRepository;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationDto;
import ru.practicum.event.dal.Event;
import ru.practicum.event.dal.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationAdminServiceImpl implements CompilationAdminService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto request) {
        log.info("createCompilation - invoked");
        Set<Event> events;
        events = (request.getEvents() != null && !request.getEvents().isEmpty()) ?
                new HashSet<>(eventRepository.findAllById(request.getEvents())) : new HashSet<>();
        Compilation compilation = Compilation.builder()
                .pinned(request.getPinned() != null && request.getPinned())
                .title(request.getTitle())
                .events(events)
                .build();
        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    @Transactional
    public String deleteCompilation(Long compId) {
        log.info("deleteCompilation(- invoked");
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation Not Found");
        }
        log.info("Result: compilation with id {} deleted ", compId);
        compilationRepository.deleteById(compId);
        return "Compilation deleted: " + compId;
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationDto updateCompilationDto) {
        log.info("updateCompilation - invoked");
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id " + compId + " not found"));
        if (updateCompilationDto.getTitle() != null) {
            compilation.setTitle(updateCompilationDto.getTitle());
        }
        if (updateCompilationDto.getPinned() != null) {
            compilation.setPinned(updateCompilationDto.getPinned());
        }
        if (updateCompilationDto.getEvents() != null && !updateCompilationDto.getEvents().isEmpty()) {
            HashSet<Event> events = new HashSet<>(eventRepository.findAllById(updateCompilationDto.getEvents()));
            compilation.setEvents(events);
        }
        Compilation updatedCompilation = compilationRepository.save(compilation);
        log.info("Result: compilation with id {} updated ", compId);
        return CompilationMapper.toCompilationDto(updatedCompilation);
    }

}