package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.category.CategoryPublicApi;
import ru.practicum.category.service.CategoryPublicService;
import ru.practicum.dto.category.CategoryDto;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@Validated
public class CategoryPublicController implements CategoryPublicApi {

    private final CategoryPublicService categoryPublicService;

    @Override
    public Collection<CategoryDto> readAllCategories(int from, int size) {
        return categoryPublicService.readAllCategories(from, size);
    }

    @Override
    public CategoryDto readCategoryById(Long catId) {
        return categoryPublicService.readCategoryById(catId);
    }

}