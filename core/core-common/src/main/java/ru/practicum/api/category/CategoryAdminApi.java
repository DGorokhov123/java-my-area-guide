package ru.practicum.api.category;

import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.validation.CreateOrUpdateValidator;

public interface CategoryAdminApi {

    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    CategoryDto addCategory(
            @RequestBody @Validated(CreateOrUpdateValidator.Create.class) CategoryDto requestCategory
    );

    @DeleteMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    String deleteCategories(
            @PathVariable @Positive Long catId
    );

    @PatchMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.OK)
    CategoryDto updateCategory(
            @PathVariable Long catId,
            @RequestBody @Validated(CreateOrUpdateValidator.Update.class) CategoryDto categoryDto
    );

}