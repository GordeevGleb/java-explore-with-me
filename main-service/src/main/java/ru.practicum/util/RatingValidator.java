package ru.practicum.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RatingValidatorImpl.class)
public @interface RatingValidator {
    String message() default "Значение должно быть null или числом больше 0 и меньше или равно 5";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
