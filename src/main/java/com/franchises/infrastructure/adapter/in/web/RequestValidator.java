package com.franchises.infrastructure.adapter.in.web;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

/**
 * Valida los DTO de entrada de forma reactiva usando Bean Validation.
 */
@Component
@RequiredArgsConstructor
public class RequestValidator {

    private final Validator validator;

    public <T> Mono<T> validate(T target) {
        var violations = validator.validate(target);
        return violations.isEmpty()
                ? Mono.just(target)
                : Mono.error(new IllegalArgumentException(violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .sorted()
                        .collect(Collectors.joining(", "))));
    }
}
