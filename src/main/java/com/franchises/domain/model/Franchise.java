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
 */
@Value
@Builder(toBuilder = true)
public class Franchise {

    String id;

    @With
    String name;

    @Builder.Default
    List<Branch> branches = List.of();

    public boolean hasBranch(String branchName) {
        return branches.stream().anyMatch(branch -> branch.getName().equals(branchName));
    }

    public Optional<Branch> findBranch(String branchName) {
        return branches.stream()
                .filter(branch -> branch.getName().equals(branchName))
                .findFirst();
    }

    public Franchise addBranch(Branch branch) {
        return toBuilder()
                .branches(Stream.concat(branches.stream(), Stream.of(branch)).toList())
                .build();
    }

    /**
     * Sustituye la sucursal identificada por {@code branchName} por {@code replacement}.
     */
    public Franchise replaceBranch(String branchName, Branch replacement) {
        return toBuilder()
                .branches(branches.stream()
                        .map(branch -> branch.getName().equals(branchName) ? replacement : branch)
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
