package ru.practicum.validation;

import ru.practicum.compilation.NewCompilationDto;

import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

public class NewCompilationCreateValidator implements CreateOrUpdateValidator.Create {
    private final Validator validator;

    public NewCompilationCreateValidator(Validator validator) {
        this.validator = validator;
    }

    public void validate(NewCompilationDto newCompilationDto) {
        Set<String> errors = validator.validate(newCompilationDto, CreateOrUpdateValidator.Create.class)
                .stream()
                .map(constraintViolation -> constraintViolation.getPropertyPath().toString() + ": " + constraintViolation.getMessage())
                .collect(Collectors.toSet());

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Validation errors: " + errors);
        }
    }
}