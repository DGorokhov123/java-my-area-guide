package ru.practicum.category.service;

import ru.practicum.category.dal.Category;
import ru.practicum.dto.category.CategoryDto;

import java.util.List;
import java.util.stream.Collectors;

public class CategoryMapper {

    public static CategoryDto toCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public static Category toCategories(CategoryDto categoryDto) {
        return Category.builder()
                .name(categoryDto.getName())
                .build();
    }

    public static List<CategoryDto> toListCategoriesDto(List<Category> list) {
        return list.stream().map(CategoryMapper::toCategoryDto).collect(Collectors.toList());
    }

}
