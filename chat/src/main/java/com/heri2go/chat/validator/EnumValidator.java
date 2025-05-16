package com.heri2go.chat.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class EnumValidator implements ConstraintValidator<Enum, java.lang.Enum> {
    private Enum annotation;

    @Override
    public void initialize(Enum constraintAnnotation) {
        this.annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(java.lang.Enum value, ConstraintValidatorContext context) {
        if (value == null)
            return false;

        Class<?> reflectionEnumClass = value.getDeclaringClass();
        return Arrays.asList(reflectionEnumClass.getEnumConstants()).contains(value);
    }
}
