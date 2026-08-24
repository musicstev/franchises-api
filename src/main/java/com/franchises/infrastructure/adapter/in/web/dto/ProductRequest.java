package com.franchises.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductRequest(
        @NotBlank(message = "el nombre del producto es obligatorio") String name,
        @NotNull(message = "el stock es obligatorio")
        @PositiveOrZero(message = "el stock no puede ser negativo") Integer stock) {

    public ProductRequest {
        name = name == null ? null : name.strip();
    }
}
