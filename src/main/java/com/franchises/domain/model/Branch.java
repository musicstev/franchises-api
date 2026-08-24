package com.franchises.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * Sucursal de una franquicia. Inmutable: toda modificación produce una nueva instancia.
 *
 * <p>{@code id} es la identidad estable de la sucursal (generada por el servidor al
 * crearla); {@code name} es un atributo de negocio mutable, no una clave. Los productos
 * se buscan y modifican por {@code id}; el nombre solo se usa para la regla de negocio
 * de no permitir nombres duplicados dentro de la misma sucursal.
 */
@Value
@Builder(toBuilder = true)
public class Branch {

    String id;

    @With
    String name;

    @Builder.Default
    List<Product> products = List.of();

    public boolean hasProductNamed(String productName) {
        return products.stream().anyMatch(product -> product.getName().equalsIgnoreCase(productName));
    }

    public Optional<Product> findProductById(String productId) {
        return products.stream()
                .filter(product -> product.getId().equals(productId))
                .findFirst();
    }

    public Branch addProduct(Product product) {
        return toBuilder()
                .products(Stream.concat(products.stream(), Stream.of(product)).toList())
                .build();
    }

    public Optional<Branch> removeProductById(String productId) {
        return findProductById(productId)
                .map(found -> toBuilder()
                        .products(products.stream()
                                .filter(product -> !product.getId().equals(productId))
                                .toList())
                        .build());
    }

    public Optional<Branch> updateProductById(String productId, UnaryOperator<Product> update) {
        return findProductById(productId)
                .map(found -> toBuilder()
                        .products(products.stream()
                                .map(product -> product.getId().equals(productId)
                                        ? update.apply(product)
                                        : product)
                                .toList())
                        .build());
    }

    /**
     * Sustituye el producto con el mismo id de {@code updated}. Para usar únicamente
     * cuando su existencia ya fue confirmada (p. ej. tras {@link #findProductById}),
     * evitando así una segunda comprobación de existencia inalcanzable en la práctica.
     */
    public Branch replaceProduct(Product updated) {
        return toBuilder()
                .products(products.stream()
                        .map(product -> product.getId().equals(updated.getId()) ? updated : product)
                        .toList())
                .build();
    }

    public Optional<Product> topStockProduct() {
        return products.stream().max(Comparator.comparingInt(Product::getStock));
    }
}
