package com.franchises.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BranchTest {

    private final Product cafe = Product.builder().name("Café").stock(10).build();
    private final Product pan = Product.builder().name("Pan").stock(25).build();

    private final Branch branch = Branch.builder()
            .name("Sucursal Centro")
            .products(List.of(cafe, pan))
            .build();

    @Test
    @DisplayName("hasProduct identifica productos existentes e inexistentes")
    void hasProduct() {
        assertThat(branch.hasProduct("Café")).isTrue();
        assertThat(branch.hasProduct("Leche")).isFalse();
    }

    @Test
    @DisplayName("findProduct devuelve el producto por nombre")
    void findProduct() {
        assertThat(branch.findProduct("Pan")).contains(pan);
        assertThat(branch.findProduct("Leche")).isEmpty();
    }

    @Test
    @DisplayName("addProduct devuelve una nueva sucursal con el producto agregado")
    void addProduct() {
        Product leche = Product.builder().name("Leche").stock(5).build();

        Branch updated = branch.addProduct(leche);

        assertThat(updated.getProducts()).containsExactly(cafe, pan, leche);
        assertThat(branch.getProducts()).containsExactly(cafe, pan);
    }

    @Test
    @DisplayName("removeProduct elimina el producto cuando existe")
    void removeProduct() {
        Optional<Branch> updated = branch.removeProduct("Café");

        assertThat(updated).isPresent();
        assertThat(updated.orElseThrow().getProducts()).containsExactly(pan);
    }

    @Test
    @DisplayName("removeProduct devuelve vacío cuando el producto no existe")
    void removeProductNotFound() {
        assertThat(branch.removeProduct("Leche")).isEmpty();
    }

    @Test
    @DisplayName("updateProduct transforma únicamente el producto indicado")
    void updateProduct() {
        Optional<Branch> updated = branch.updateProduct("Café", product -> product.withStock(99));

        assertThat(updated).isPresent();
        assertThat(updated.orElseThrow().findProduct("Café").orElseThrow().getStock()).isEqualTo(99);
        assertThat(updated.orElseThrow().findProduct("Pan").orElseThrow().getStock()).isEqualTo(25);
    }

    @Test
    @DisplayName("updateProduct devuelve vacío cuando el producto no existe")
    void updateProductNotFound() {
        assertThat(branch.updateProduct("Leche", product -> product.withStock(1))).isEmpty();
    }

    @Test
    @DisplayName("topStockProduct devuelve el producto con mayor stock")
    void topStockProduct() {
        assertThat(branch.topStockProduct()).contains(pan);
    }

    @Test
    @DisplayName("topStockProduct devuelve vacío para una sucursal sin productos")
    void topStockProductEmpty() {
        Branch empty = Branch.builder().name("Vacía").build();

        assertThat(empty.topStockProduct()).isEmpty();
        assertThat(empty.getProducts()).isEmpty();
    }
}
