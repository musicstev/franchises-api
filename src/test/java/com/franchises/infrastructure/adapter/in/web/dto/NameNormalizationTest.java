package com.franchises.infrastructure.adapter.in.web.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica que los DTO de entrada recorten espacios en blanco del nombre al deserializar
 * (constructor compacto), y que toleren {@code null} sin lanzar excepción — @NotBlank se
 * encarga de rechazarlo después, en la fase de validación.
 */
class NameNormalizationTest {

    @Test
    @DisplayName("BranchRequest recorta espacios y tolera nombre nulo")
    void branchRequestTrimsAndToleratesNull() {
        assertThat(new BranchRequest("  Centro  ").name()).isEqualTo("Centro");
        assertThat(new BranchRequest(null).name()).isNull();
    }

    @Test
    @DisplayName("ProductRequest recorta espacios y tolera nombre nulo")
    void productRequestTrimsAndToleratesNull() {
        assertThat(new ProductRequest("  Café  ", 10).name()).isEqualTo("Café");
        assertThat(new ProductRequest(null, 10).name()).isNull();
    }

    @Test
    @DisplayName("NameUpdateRequest recorta espacios y tolera nombre nulo")
    void nameUpdateRequestTrimsAndToleratesNull() {
        assertThat(new NameUpdateRequest("  Nueva Marca  ").name()).isEqualTo("Nueva Marca");
        assertThat(new NameUpdateRequest(null).name()).isNull();
    }

    @Test
    @DisplayName("FranchiseRequest recorta espacios y tolera nombre nulo")
    void franchiseRequestTrimsAndToleratesNull() {
        assertThat(new FranchiseRequest("  Mi Franquicia  ").name()).isEqualTo("Mi Franquicia");
        assertThat(new FranchiseRequest(null).name()).isNull();
    }
}
