package com.franchises.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FranchiseTest {

    private final Branch centro = Branch.builder()
            .id("b-1")
            .name("Centro")
            .products(List.of(
                    Product.builder().id("p-1").name("Café").stock(10).build(),
                    Product.builder().id("p-2").name("Pan").stock(25).build()))
            .build();

    private final Branch norte = Branch.builder()
            .id("b-2")
            .name("Norte")
            .products(List.of(Product.builder().id("p-3").name("Leche").stock(7).build()))
            .build();

    private final Branch vacia = Branch.builder().id("b-3").name("Vacía").build();

    private final Franchise franchise = Franchise.builder()
            .id("f-1")
            .name("Mi Franquicia")
            .branches(List.of(centro, norte, vacia))
            .build();

    @Test
    @DisplayName("hasBranchNamed identifica sucursales existentes e inexistentes por nombre")
    void hasBranchNamed() {
        assertThat(franchise.hasBranchNamed("Centro")).isTrue();
        assertThat(franchise.hasBranchNamed("Sur")).isFalse();
    }

    @Test
    @DisplayName("hasBranchNamed ignora mayúsculas y minúsculas")
    void hasBranchNamedIsCaseInsensitive() {
        assertThat(franchise.hasBranchNamed("CENTRO")).isTrue();
        assertThat(franchise.hasBranchNamed("centro")).isTrue();
    }

    @Test
    @DisplayName("findBranchById devuelve la sucursal por id")
    void findBranchById() {
        assertThat(franchise.findBranchById("b-2")).contains(norte);
        assertThat(franchise.findBranchById("b-999")).isEmpty();
    }

    @Test
    @DisplayName("addBranch devuelve una nueva franquicia con la sucursal agregada")
    void addBranch() {
        Branch sur = Branch.builder().id("b-4").name("Sur").build();

        Franchise updated = franchise.addBranch(sur);

        assertThat(updated.getBranches()).containsExactly(centro, norte, vacia, sur);
        assertThat(franchise.getBranches()).containsExactly(centro, norte, vacia);
    }

    @Test
    @DisplayName("replaceBranchById sustituye únicamente la sucursal indicada")
    void replaceBranchById() {
        Branch renamed = centro.withName("Centro Histórico");

        Franchise updated = franchise.replaceBranchById("b-1", renamed);

        assertThat(updated.findBranchById("b-1").orElseThrow().getName()).isEqualTo("Centro Histórico");
        assertThat(updated.findBranchById("b-2")).contains(norte);
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
