package ru.practicum.compilation;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CompilationAdminServiceImpl implements CompilationAdminService {

    CompilationRepository compilationRepository;
    EventRepository eventRepository;

    @Override
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
    public void deleteCompilation(Long compId) {
        log.info("deleteCompilation(- invoked");
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation Not Found");
        }
        log.info("Result: compilation with id {} deleted ", compId);
        compilationRepository.deleteById(compId);
    }

    @Override
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