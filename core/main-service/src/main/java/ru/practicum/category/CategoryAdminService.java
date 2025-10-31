package ru.practicum.category;

public interface CategoryAdminService {

    CategoryDto createCategory(CategoryDto requestCategory);

    void deleteCategory(Long catId);

    CategoryDto updateCategory(Long catId, CategoryDto categoryDto);

}
