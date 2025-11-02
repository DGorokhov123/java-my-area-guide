package ru.practicum.compilation;

public interface CompilationAdminService {

    CompilationDto createCompilation(NewCompilationDto request);

    void deleteCompilation(Long compId);

    CompilationDto updateCompilation(Long compId, UpdateCompilationDto updateCompilationDto);

}
