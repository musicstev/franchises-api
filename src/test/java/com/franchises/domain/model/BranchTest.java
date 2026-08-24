package com.franchises.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BranchTest {

    private final Product cafe = Product.builder().id("p-1").name("Café").stock(10).build();
    private final Product pan = Product.builder().id("p-2").name("Pan").stock(25).build();

    private final Branch branch = Branch.builder()
            .id("b-1")
            .name("Sucursal Centro")
            .products(List.of(cafe, pan))
            .build();

    @Test
    @DisplayName("hasProductNamed identifica productos existentes e inexistentes por nombre")
    void hasProductNamed() {
        assertThat(branch.hasProductNamed("Café")).isTrue();
        assertThat(branch.hasProductNamed("Leche")).isFalse();
    }

    @Test
    @DisplayName("hasProductNamed ignora mayúsculas y minúsculas")
    void hasProductNamedIsCaseInsensitive() {
        assertThat(branch.hasProductNamed("CAFÉ")).isTrue();
        assertThat(branch.hasProductNamed("café")).isTrue();
    }

    @Test
    @DisplayName("findProductById devuelve el producto por id")
    void findProductById() {
        assertThat(branch.findProductById("p-2")).contains(pan);
        assertThat(branch.findProductById("p-999")).isEmpty();
    }

    @Test
    @DisplayName("addProduct devuelve una nueva sucursal con el producto agregado")
    void addProduct() {
        Product leche = Product.builder().id("p-3").name("Leche").stock(5).build();

        Branch updated = branch.addProduct(leche);

        assertThat(updated.getProducts()).containsExactly(cafe, pan, leche);
        assertThat(branch.getProducts()).containsExactly(cafe, pan);
    }

    @Test
    @DisplayName("removeProductById elimina el producto cuando existe")
    void removeProductById() {
        Optional<Branch> updated = branch.removeProductById("p-1");

        assertThat(updated).isPresent();
        assertThat(updated.orElseThrow().getProducts()).containsExactly(pan);
    }

    @Test
    @DisplayName("removeProductById devuelve vacío cuando el producto no existe")
    void removeProductByIdNotFound() {
        assertThat(branch.removeProductById("p-999")).isEmpty();
    }

    @Test
    @DisplayName("updateProductById transforma únicamente el producto indicado")
    void updateProductById() {
        Optional<Branch> updated = branch.updateProductById("p-1", product -> product.withStock(99));

        assertThat(updated).isPresent();
        assertThat(updated.orElseThrow().findProductById("p-1").orElseThrow().getStock()).isEqualTo(99);
        assertThat(updated.orElseThrow().findProductById("p-2").orElseThrow().getStock()).isEqualTo(25);
    }

    @Test
    @DisplayName("updateProductById devuelve vacío cuando el producto no existe")
    void updateProductByIdNotFound() {
        assertThat(branch.updateProductById("p-999", product -> product.withStock(1))).isEmpty();
    }

    @Test
    @DisplayName("replaceProduct sustituye únicamente el producto con el mismo id")
    void replaceProduct() {
        Branch updated = branch.replaceProduct(cafe.withName("Café Premium"));

        assertThat(updated.findProductById("p-1").orElseThrow().getName()).isEqualTo("Café Premium");
        assertThat(updated.findProductById("p-2").orElseThrow().getName()).isEqualTo("Pan");
    }

    @Test
    @DisplayName("topStockProduct devuelve el producto con mayor stock")
    void topStockProduct() {
        assertThat(branch.topStockProduct()).contains(pan);
    }

    @Test
    @DisplayName("topStockProduct devuelve vacío para una sucursal sin productos")
    void topStockProductEmpty() {
        Branch empty = Branch.builder().id("b-2").name("Vacía").build();

        assertThat(empty.topStockProduct()).isEmpty();
        assertThat(empty.getProducts()).isEmpty();
    }
}
