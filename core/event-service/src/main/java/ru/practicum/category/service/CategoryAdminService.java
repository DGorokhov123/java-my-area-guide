package ru.practicum.category.service;

import ru.practicum.dto.category.CategoryDto;

public interface CategoryAdminService {

    CategoryDto createCategory(CategoryDto requestCategory);

    String deleteCategory(Long catId);

    CategoryDto updateCategory(Long catId, CategoryDto categoryDto);

}
