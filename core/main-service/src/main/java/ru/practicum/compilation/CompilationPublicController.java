package ru.practicum.compilation;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/compilations")
@RequiredArgsConstructor
@Slf4j
public class CompilationPublicController {

    private final CompilationPublicService compilationPublicService;

    @GetMapping
    public ResponseEntity<List<CompilationDto>> getCompilation(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size
    ) {
        log.info("Calling the GET request to /compilations endpoint");
        List<CompilationDto> list = compilationPublicService.readAllCompilations(pinned, from, size);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{compId}")
    public ResponseEntity<CompilationDto> getCompilationById(
            @PathVariable Long compId
    ) {
        log.info("Calling the GET request to /compilations/{compId} endpoint");
        CompilationDto response = compilationPublicService.readCompilationById(compId);
        return ResponseEntity.ok(response);
    }

}
