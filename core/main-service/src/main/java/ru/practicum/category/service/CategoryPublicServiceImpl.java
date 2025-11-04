package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.exception.NotFoundException;

import java.util.List;

import static ru.practicum.util.Util.createPageRequestAsc;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CategoryPublicServiceImpl implements CategoryPublicService {

    private final CategoryRepository repository;

    @Override
    public List<CategoryDto> readAllCategories(Integer from, Integer size) {
        log.info("readAllCategories - invoked");
        Page<Category> page = repository.findAll(createPageRequestAsc(from, size));
        List<Category> cat = page.getContent();
        log.info("Result: categories size = {}", cat.size());
        return CategoryMapper.toListCategoriesDto(cat);
    }

    @Override
    public CategoryDto readCategoryById(Long catId) {
        log.info("readCategoryById - invoked");
        Category category = repository.findById(catId).orElseThrow(() -> {
            log.error("Category with id = {} not exist", catId);
            return new NotFoundException("Category not found");
        });
        log.info("Result: received a category - {}", category.getName());
        return CategoryMapper.toCategoryDto(category);
    }
}