package ru.practicum.api.compilation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationDto;

public interface CompilationAdminApi {

    @PostMapping("/admin/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    CompilationDto postCompilations(
            @RequestBody @Valid NewCompilationDto newCompilationDto
    );

    @DeleteMapping("/admin/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    String deleteCompilation(
            @PathVariable Long compId
    );

    @PatchMapping("/admin/compilations/{compId}")
    @ResponseStatus(HttpStatus.OK)
    CompilationDto patchCompilation(
            @PathVariable Long compId,
            @RequestBody @Valid UpdateCompilationDto updateCompilationDto
    );

}