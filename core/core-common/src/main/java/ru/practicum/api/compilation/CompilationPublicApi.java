package ru.practicum.api.compilation;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.dto.compilation.CompilationDto;

import java.util.Collection;

public interface CompilationPublicApi {

    @GetMapping("/compilations")
    @ResponseStatus(HttpStatus.OK)
    Collection<CompilationDto> getCompilation(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size
    );

    @GetMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.OK)
    CompilationDto getCompilationById(
            @PathVariable Long compId
    );

}
