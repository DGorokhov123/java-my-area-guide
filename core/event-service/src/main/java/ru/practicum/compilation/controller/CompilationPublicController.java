package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.compilation.CompilationPublicApi;
import ru.practicum.compilation.service.CompilationPublicService;
import ru.practicum.dto.compilation.CompilationDto;

import java.util.Collection;

@RestController
@Validated
@RequiredArgsConstructor
public class CompilationPublicController implements CompilationPublicApi {

    private final CompilationPublicService compilationPublicService;

    @Override
    public Collection<CompilationDto> getCompilation(Boolean pinned, int from, int size) {
        return compilationPublicService.readAllCompilations(pinned, from, size);
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        return compilationPublicService.readCompilationById(compId);
    }

}