package ru.practicum.compilation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
@Slf4j
public class CompilationAdminController {

    private final CompilationAdminService compilationAdminService;

    @PostMapping
    public ResponseEntity<CompilationDto> postCompilations(
            @RequestBody @Valid NewCompilationDto newCompilationDto
    ) {
        log.info("Calling the POST request to /admin/compilations endpoint");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(compilationAdminService.createCompilation(newCompilationDto));
    }

    @DeleteMapping("/{compId}")
    public ResponseEntity<String> deleteCompilation(
            @PathVariable Long compId
    ) {
        log.info("Calling the DELETE request to /admin/endpoint/{compId}");
        compilationAdminService.deleteCompilation(compId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body("Compilation deleted: " + compId);
    }

    @PatchMapping("/{compId}")
    public ResponseEntity<CompilationDto> patchCompilation(
            @PathVariable Long compId,
            @RequestBody @Valid UpdateCompilationDto updateCompilationDto
    ) {
        log.info("Calling the PATCH request to /admin/compilations/{compId} endpoint");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(compilationAdminService.updateCompilation(compId, updateCompilationDto));
    }
}