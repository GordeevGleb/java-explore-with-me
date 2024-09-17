package ru.practicum.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RatingValidatorImpl implements ConstraintValidator<RatingValidator, Float> {

    @Override
    public boolean isValid(Float value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value > 0 && value <= 5;
    }
}
