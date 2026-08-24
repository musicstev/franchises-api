package com.franchises.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record StockUpdateRequest(
        @NotNull(message = "el stock es obligatorio")
        @PositiveOrZero(message = "el stock no puede ser negativo") Integer stock) {
}
