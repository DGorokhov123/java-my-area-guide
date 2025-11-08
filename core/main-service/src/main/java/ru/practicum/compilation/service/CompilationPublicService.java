package ru.practicum.compilation.service;

import ru.practicum.dto.compilation.CompilationDto;

import java.util.List;

public interface CompilationPublicService {

    CompilationDto readCompilationById(Long compId);

    List<CompilationDto> readAllCompilations(Boolean pinned, int from, int size);

}
