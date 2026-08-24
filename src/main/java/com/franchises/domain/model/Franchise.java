package com.franchises.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Raíz de agregado del dominio: una franquicia con sus sucursales.
 * Inmutable: toda modificación produce una nueva instancia.
 *
 * <p>Las sucursales se buscan y modifican por {@code id}; el nombre solo se usa para la
 * regla de negocio de no permitir nombres duplicados dentro de la misma franquicia.
 */
@Value
@Builder(toBuilder = true)
public class Franchise {

    String id;

    /**
     * Token de concurrencia del agregado. Es {@code null} mientras la franquicia no se
     * haya persistido y se propaga sin cambios a través de cada mutación, de modo que
     * la escritura pueda detectar modificaciones concurrentes.
     */
    Long version;

    @With
    String name;

    @Builder.Default
    List<Branch> branches = List.of();

    public boolean hasBranchNamed(String branchName) {
        return branches.stream().anyMatch(branch -> branch.getName().equals(branchName));
    }

    public Optional<Branch> findBranchById(String branchId) {
        return branches.stream()
                .filter(branch -> branch.getId().equals(branchId))
                .findFirst();
    }

    public Franchise addBranch(Branch branch) {
        return toBuilder()
                .branches(Stream.concat(branches.stream(), Stream.of(branch)).toList())
                .build();
    }

    /**
     * Sustituye la sucursal identificada por {@code branchId} por {@code replacement}.
     */
    public Franchise replaceBranchById(String branchId, Branch replacement) {
        return toBuilder()
                .branches(branches.stream()
                        .map(branch -> branch.getId().equals(branchId) ? replacement : branch)
                        .toList())
                .build();
    }

    /**
     * Producto con mayor stock por cada sucursal (se omiten sucursales sin productos).
     */
    public List<TopStockProduct> topStockProducts() {
        return branches.stream()
                .flatMap(branch -> branch.topStockProduct()
                        .map(product -> new TopStockProduct(branch.getName(), product.getName(), product.getStock()))
                        .stream())
                .toList();
    }
}
