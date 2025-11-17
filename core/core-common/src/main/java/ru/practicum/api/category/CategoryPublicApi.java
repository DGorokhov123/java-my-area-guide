package ru.practicum.api.category;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.dto.category.CategoryDto;

import java.util.Collection;

public interface CategoryPublicApi {

    @GetMapping("/categories")
    @ResponseStatus(HttpStatus.OK)
    Collection<CategoryDto> readAllCategories(
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size
    );

    @GetMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.OK)
    CategoryDto readCategoryById(
            @PathVariable Long catId
    );

}