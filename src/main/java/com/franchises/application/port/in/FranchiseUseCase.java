package com.franchises.application.port.in;

import com.franchises.domain.model.Franchise;
import com.franchises.domain.model.TopStockProduct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada: operaciones de negocio sobre franquicias.
 *
 * <p>Las sucursales y productos se identifican por {@code branchId}/{@code productId}
 * (generados por el servidor al crearlos), no por su nombre: el nombre es un atributo de
 * negocio mutable, no una identidad estable de recurso.
 */
public interface FranchiseUseCase {

    Mono<Franchise> createFranchise(String name);

    Mono<Franchise> updateFranchiseName(String franchiseId, String newName);

    Mono<Franchise> addBranch(String franchiseId, String branchName);

    Mono<Franchise> updateBranchName(String franchiseId, String branchId, String newName);

    Mono<Franchise> addProduct(String franchiseId, String branchId, String productName, int stock);

    Mono<Franchise> removeProduct(String franchiseId, String branchId, String productId);

    Mono<Franchise> updateProductStock(String franchiseId, String branchId, String productId, int stock);

    Mono<Franchise> updateProductName(String franchiseId, String branchId, String productId, String newName);

    Flux<TopStockProduct> topStockProducts(String franchiseId);
}
