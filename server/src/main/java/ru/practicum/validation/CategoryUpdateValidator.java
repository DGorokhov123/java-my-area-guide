package ru.practicum.validation;

import ru.practicum.category.CategoryDto;

import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

public class CategoryUpdateValidator implements CreateOrUpdateValidator.Update {
    private final Validator validator;

    public CategoryUpdateValidator(Validator validator) {
        this.validator = validator;
    }

    public void validate(CategoryDto categoryDto) {
        Set<String> errors = validator.validate(categoryDto, CreateOrUpdateValidator.Create.class)
                .stream()
                .map(constraintViolation -> constraintViolation.getPropertyPath().toString() + ": " + constraintViolation.getMessage())
                .collect(Collectors.toSet());

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Validation errors: " + errors);
        }
    }
}
