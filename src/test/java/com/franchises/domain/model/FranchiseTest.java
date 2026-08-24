package com.franchises.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FranchiseTest {

    private final Branch centro = Branch.builder()
            .name("Centro")
            .products(List.of(
                    Product.builder().name("Café").stock(10).build(),
                    Product.builder().name("Pan").stock(25).build()))
            .build();

    private final Branch norte = Branch.builder()
            .name("Norte")
            .products(List.of(Product.builder().name("Leche").stock(7).build()))
            .build();

    private final Branch vacia = Branch.builder().name("Vacía").build();

    private final Franchise franchise = Franchise.builder()
            .id("f-1")
            .name("Mi Franquicia")
            .branches(List.of(centro, norte, vacia))
            .build();

    @Test
    @DisplayName("hasBranch identifica sucursales existentes e inexistentes")
    void hasBranch() {
        assertThat(franchise.hasBranch("Centro")).isTrue();
        assertThat(franchise.hasBranch("Sur")).isFalse();
    }

    @Test
    @DisplayName("findBranch devuelve la sucursal por nombre")
    void findBranch() {
        assertThat(franchise.findBranch("Norte")).contains(norte);
        assertThat(franchise.findBranch("Sur")).isEmpty();
    }

    @Test
    @DisplayName("addBranch devuelve una nueva franquicia con la sucursal agregada")
    void addBranch() {
        Branch sur = Branch.builder().name("Sur").build();

        Franchise updated = franchise.addBranch(sur);

        assertThat(updated.getBranches()).containsExactly(centro, norte, vacia, sur);
        assertThat(franchise.getBranches()).containsExactly(centro, norte, vacia);
    }

    @Test
    @DisplayName("replaceBranch sustituye únicamente la sucursal indicada")
    void replaceBranch() {
        Branch renamed = centro.withName("Centro Histórico");

        Franchise updated = franchise.replaceBranch("Centro", renamed);

        assertThat(updated.findBranch("Centro Histórico")).isPresent();
        assertThat(updated.findBranch("Centro")).isEmpty();
        assertThat(updated.findBranch("Norte")).contains(norte);
    }

    @Test
    @DisplayName("topStockProducts devuelve el producto de mayor stock por sucursal, omitiendo sucursales vacías")
    void topStockProducts() {
        List<TopStockProduct> result = franchise.topStockProducts();

        assertThat(result).containsExactly(
                new TopStockProduct("Centro", "Pan", 25),
                new TopStockProduct("Norte", "Leche", 7));
    }

    @Test
    @DisplayName("withName devuelve una nueva franquicia renombrada")
    void withName() {
        Franchise renamed = franchise.withName("Nueva Marca");

        assertThat(renamed.getName()).isEqualTo("Nueva Marca");
        assertThat(renamed.getId()).isEqualTo("f-1");
        assertThat(franchise.getName()).isEqualTo("Mi Franquicia");
    }
}
