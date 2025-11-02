package ru.practicum.category;

import java.util.List;

public interface CategoryPublicService {

    List<CategoryDto> readAllCategories(Integer from, Integer size);

    CategoryDto readCategoryById(Long catId);

}
