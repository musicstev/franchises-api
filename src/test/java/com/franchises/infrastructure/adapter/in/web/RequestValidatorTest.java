package com.franchises.infrastructure.adapter.in.web;

import com.franchises.infrastructure.adapter.in.web.dto.ProductRequest;
import jakarta.validation.Validation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class RequestValidatorTest {

    private final RequestValidator validator = new RequestValidator(
            Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    @DisplayName("emite el objeto cuando es válido")
    void valid() {
        ProductRequest request = new ProductRequest("Café", 10);

        StepVerifier.create(validator.validate(request))
                .expectNext(request)
                .verifyComplete();
    }

    @Test
    @DisplayName("falla con IllegalArgumentException y los mensajes de violación cuando es inválido")
    void invalid() {
        ProductRequest request = new ProductRequest("  ", -5);

        StepVerifier.create(validator.validate(request))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(IllegalArgumentException.class);
                    assertThat(error.getMessage())
                            .contains("el nombre del producto es obligatorio")
                            .contains("el stock no puede ser negativo");
                })
                .verify();
    }
}
