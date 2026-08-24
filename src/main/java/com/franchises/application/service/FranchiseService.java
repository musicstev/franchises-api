package com.franchises.application.service;

import com.franchises.application.port.in.FranchiseUseCase;
import com.franchises.application.port.out.FranchiseRepository;
import com.franchises.domain.exception.DuplicateResourceException;
import com.franchises.domain.exception.NotFoundException;
import com.franchises.domain.model.Branch;
import com.franchises.domain.model.Franchise;
import com.franchises.domain.model.Product;
import com.franchises.domain.model.TopStockProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

@Service
@RequiredArgsConstructor
public class FranchiseService implements FranchiseUseCase {

    private final FranchiseRepository repository;

    @Override
    public Mono<Franchise> createFranchise(String name) {
        return repository.save(Franchise.builder().name(name).build());
    }

    @Override
    public Mono<Franchise> updateFranchiseName(String franchiseId, String newName) {
        return findFranchise(franchiseId)
                .map(franchise -> franchise.withName(newName))
                .flatMap(repository::save);
    }

    @Override
    public Mono<Franchise> addBranch(String franchiseId, String branchName) {
        return findFranchise(franchiseId)
                .flatMap(franchise -> franchise.hasBranch(branchName)
                        ? Mono.error(duplicateBranch(branchName))
                        : repository.save(franchise.addBranch(Branch.builder().name(branchName).build())));
    }

    @Override
    public Mono<Franchise> updateBranchName(String franchiseId, String branchName, String newName) {
        return updateExistingBranch(franchiseId, branchName,
                (franchise, branch) -> franchise.hasBranch(newName) && !branchName.equals(newName)
                        ? Mono.error(duplicateBranch(newName))
                        : Mono.just(branch.withName(newName)));
    }

    @Override
    public Mono<Franchise> addProduct(String franchiseId, String branchName, String productName, int stock) {
        return updateExistingBranch(franchiseId, branchName,
                (franchise, branch) -> branch.hasProduct(productName)
                        ? Mono.error(duplicateProduct(productName, branchName))
                        : Mono.just(branch.addProduct(Product.builder().name(productName).stock(stock).build())));
    }

    @Override
    public Mono<Franchise> removeProduct(String franchiseId, String branchName, String productName) {
        return updateExistingBranch(franchiseId, branchName,
                (franchise, branch) -> branch.removeProduct(productName)
                        .map(Mono::just)
                        .orElseGet(() -> Mono.error(productNotFound(productName, branchName))));
    }

    @Override
    public Mono<Franchise> updateProductStock(String franchiseId, String branchName, String productName, int stock) {
        return updateExistingBranch(franchiseId, branchName,
                (franchise, branch) -> branch.updateProduct(productName, product -> product.withStock(stock))
                        .map(Mono::just)
                        .orElseGet(() -> Mono.error(productNotFound(productName, branchName))));
    }

    @Override
    public Mono<Franchise> updateProductName(String franchiseId, String branchName, String productName, String newName) {
        return updateExistingBranch(franchiseId, branchName,
                (franchise, branch) -> branch.hasProduct(newName) && !productName.equals(newName)
                        ? Mono.error(duplicateProduct(newName, branchName))
                        : branch.updateProduct(productName, product -> product.withName(newName))
                                .map(Mono::just)
                                .orElseGet(() -> Mono.error(productNotFound(productName, branchName))));
    }

    @Override
    public Flux<TopStockProduct> topStockProducts(String franchiseId) {
        return findFranchise(franchiseId)
                .flatMapIterable(Franchise::topStockProducts);
    }

    private Mono<Franchise> findFranchise(String franchiseId) {
        return repository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new NotFoundException("Franquicia no encontrada: " + franchiseId)));
    }

    /**
     * Localiza la sucursal, aplica la operación de negocio y persiste el agregado resultante.
     */
    private Mono<Franchise> updateExistingBranch(String franchiseId, String branchName,
            BiFunction<Franchise, Branch, Mono<Branch>> operation) {
        return findFranchise(franchiseId)
                .flatMap(franchise -> franchise.findBranch(branchName)
                        .map(branch -> operation.apply(franchise, branch)
                                .map(updated -> franchise.replaceBranch(branchName, updated))
                                .flatMap(repository::save))
                        .orElseGet(() -> Mono.error(branchNotFound(branchName))));
    }

    private NotFoundException branchNotFound(String branchName) {
        return new NotFoundException("Sucursal no encontrada: " + branchName);
    }

    private NotFoundException productNotFound(String productName, String branchName) {
        return new NotFoundException(
                "Producto no encontrado: " + productName + " en la sucursal " + branchName);
    }

    private DuplicateResourceException duplicateBranch(String branchName) {
        return new DuplicateResourceException("Ya existe una sucursal con el nombre: " + branchName);
    }

    private DuplicateResourceException duplicateProduct(String productName, String branchName) {
        return new DuplicateResourceException(
                "Ya existe un producto con el nombre " + productName + " en la sucursal " + branchName);
    }
}
