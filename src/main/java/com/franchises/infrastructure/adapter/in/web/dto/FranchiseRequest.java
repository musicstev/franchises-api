package com.franchises.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record FranchiseRequest(
        @NotBlank(message = "el nombre de la franquicia es obligatorio") String name) {
}
