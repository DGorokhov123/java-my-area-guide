package ru.practicum.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NotBlankButNullAllowedValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotBlankButNullAllowed {

    String message() default "Shouldn't be blank (but can be null)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
