package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.compilation.CompilationAdminApi;
import ru.practicum.compilation.service.CompilationAdminService;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationDto;

@RestController
@Validated
@RequiredArgsConstructor
public class CompilationAdminController implements CompilationAdminApi {

    private final CompilationAdminService compilationAdminService;

    @Override
    public CompilationDto postCompilations(NewCompilationDto newCompilationDto) {
        return compilationAdminService.createCompilation(newCompilationDto);
    }

    @Override
    public String deleteCompilation(Long compId) {
        return compilationAdminService.deleteCompilation(compId);
    }

    @Override
    public CompilationDto patchCompilation(Long compId, UpdateCompilationDto updateCompilationDto) {
        return compilationAdminService.updateCompilation(compId, updateCompilationDto);
    }

}