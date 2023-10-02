package fr.backendt.cinephobia.utils;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;

public class NotBlankIfPresentValidator implements ConstraintValidator<NotBlankIfPresentValidator.NotBlankIfPresent, String> {

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if(s == null) {
            return true;
        }
        return !s.isBlank();
    }

    @Documented
    @Constraint(validatedBy = NotBlankIfPresentValidator.class)
    @Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface NotBlankIfPresent{
        String message() default "String should not be blank";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};

    }
}
