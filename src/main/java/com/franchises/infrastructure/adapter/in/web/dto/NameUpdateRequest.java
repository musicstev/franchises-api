package com.franchises.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record NameUpdateRequest(
        @NotBlank(message = "el nombre es obligatorio") String name) {
}
