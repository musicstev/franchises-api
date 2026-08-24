package com.franchises.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record BranchRequest(
        @NotBlank(message = "el nombre de la sucursal es obligatorio") String name) {

    public BranchRequest {
        name = name == null ? null : name.strip();
    }
}
