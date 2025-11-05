package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = false)
public class CategoryAdminServiceImpl implements CategoryAdminService {

    private final CategoryRepository categoryRepository;

    private final EventRepository eventRepository;

    @Override
    public CategoryDto createCategory(CategoryDto requestCategory) {
        log.info("createCategories - invoked");
        if (categoryRepository.existsByName(requestCategory.getName())) {
            log.error("Category name not unique {}", requestCategory.getName());
            throw new ConflictException("Category with this name already exists");
        }
        Category result = categoryRepository.saveAndFlush(CategoryMapper.toCategories(requestCategory));
        log.info("Result: category - {} - saved", result.getName());
        return CategoryMapper.toCategoryDto(result);
    }

    @Override
    public void deleteCategory(Long catId) {
        log.info("deleteCategories - invoked");
        if (!categoryRepository.existsById(catId)) {
            log.error("Category with this id does not exist {}", catId);
            throw new NotFoundException("Category with this id does not exist");
        }
        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("Can't delete a category with associated events");
        }
        log.info("Result: category with id - {} - deleted", catId);
        categoryRepository.deleteById(catId);
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        log.info("updateCategories - invoked");
        Category category = categoryRepository.findById(catId).orElseThrow(()
                -> new NotFoundException("This Category not found"));
        if (!category.getName().equals(categoryDto.getName()) &&
                categoryRepository.existsByName(categoryDto.getName())) {
            log.error("Category with this name not unique: {}", categoryDto.getName());
            throw new ConflictException("Category with this name not unique: " + categoryDto.getName());
        }
        category.setName(categoryDto.getName());
        log.info("Result: category - {} updated", category.getName());
        return CategoryMapper.toCategoryDto(category);
    }
}