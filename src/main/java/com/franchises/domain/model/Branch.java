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
 */
@Value
@Builder(toBuilder = true)
public class Branch {

    @With
    String name;

    @Builder.Default
    List<Product> products = List.of();

    public boolean hasProduct(String productName) {
        return products.stream().anyMatch(product -> product.getName().equals(productName));
    }

    public Optional<Product> findProduct(String productName) {
        return products.stream()
                .filter(product -> product.getName().equals(productName))
                .findFirst();
    }

    public Branch addProduct(Product product) {
        return toBuilder()
                .products(Stream.concat(products.stream(), Stream.of(product)).toList())
                .build();
    }

    public Optional<Branch> removeProduct(String productName) {
        return findProduct(productName)
                .map(found -> toBuilder()
                        .products(products.stream()
                                .filter(product -> !product.getName().equals(productName))
                                .toList())
                        .build());
    }

    public Optional<Branch> updateProduct(String productName, UnaryOperator<Product> update) {
        return findProduct(productName)
                .map(found -> toBuilder()
                        .products(products.stream()
                                .map(product -> product.getName().equals(productName)
                                        ? update.apply(product)
                                        : product)
                                .toList())
                        .build());
    }

    public Optional<Product> topStockProduct() {
        return products.stream().max(Comparator.comparingInt(Product::getStock));
    }
}
