package ru.practicum.category.service;

import ru.practicum.category.dto.CategoryDto;

public interface CategoryAdminService {

    CategoryDto createCategory(CategoryDto requestCategory);

    void deleteCategory(Long catId);

    CategoryDto updateCategory(Long catId, CategoryDto categoryDto);

}
