package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.category.CategoryAdminApi;
import ru.practicum.category.service.CategoryAdminService;
import ru.practicum.dto.category.CategoryDto;

@RestController
@RequiredArgsConstructor
@Validated
public class CategoryAdminController implements CategoryAdminApi {

    private final CategoryAdminService categoryAdminService;

    @Override
    public CategoryDto addCategory(CategoryDto requestCategory) {
        return categoryAdminService.createCategory(requestCategory);
    }

    @Override
    public String deleteCategories(Long catId) {
        return categoryAdminService.deleteCategory(catId);
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        return categoryAdminService.updateCategory(catId, categoryDto);
    }

}
