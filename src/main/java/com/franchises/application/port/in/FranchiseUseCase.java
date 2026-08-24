package com.franchises.application.port.in;

import com.franchises.domain.model.Franchise;
import com.franchises.domain.model.TopStockProduct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada: operaciones de negocio sobre franquicias.
 */
public interface FranchiseUseCase {

    Mono<Franchise> createFranchise(String name);

    Mono<Franchise> updateFranchiseName(String franchiseId, String newName);

    Mono<Franchise> addBranch(String franchiseId, String branchName);

    Mono<Franchise> updateBranchName(String franchiseId, String branchName, String newName);

    Mono<Franchise> addProduct(String franchiseId, String branchName, String productName, int stock);

    Mono<Franchise> removeProduct(String franchiseId, String branchName, String productName);

    Mono<Franchise> updateProductStock(String franchiseId, String branchName, String productName, int stock);

    Mono<Franchise> updateProductName(String franchiseId, String branchName, String productName, String newName);

    Flux<TopStockProduct> topStockProducts(String franchiseId);
}
