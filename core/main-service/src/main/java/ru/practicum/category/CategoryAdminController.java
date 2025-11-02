package ru.practicum.category;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.validation.CreateOrUpdateValidator;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/admin/categories")
@Slf4j
public class CategoryAdminController {

    private final CategoryAdminService categoryAdminService;

    @PostMapping
    public ResponseEntity<CategoryDto> addCategory(
            @RequestBody @Validated(CreateOrUpdateValidator.Create.class)
            CategoryDto requestCategory,
            BindingResult bindingResult
    ) {
        log.info("Calling the POST request to /admin/categories endpoint");
        if (bindingResult.hasErrors()) {
            log.error("Validation error with category name");
            return ResponseEntity.badRequest().body((requestCategory));
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryAdminService.createCategory(requestCategory));
    }

    @DeleteMapping("/{catId}")
    public ResponseEntity<String> deleteCategories(
            @PathVariable @Positive Long catId
    ) {
        log.info("Calling the DELETE request to /admin/categories/{catId} endpoint");
        categoryAdminService.deleteCategory(catId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body("Category deleted: " + catId);
    }

    @PatchMapping("/{catId}")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long catId,
            @RequestBody @Validated(CreateOrUpdateValidator.Update.class) CategoryDto categoryDto
    ) {
        log.info("Calling the PATCH request to /admin/categories/{catId} endpoint");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryAdminService.updateCategory(catId, categoryDto));
    }

}
