package com.example.tournament.util.validation;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DataValidator {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public <T> ValidationResult validate(T data) {

        Set<ConstraintViolation<T>> constraintViolations = validator.validate(data);

        if (CollectionUtils.isEmpty(constraintViolations)) {
            return ValidationResult.valid();
        }

        String errorMessage = constraintViolations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));

        return ValidationResult.withError(errorMessage);
    }
}
