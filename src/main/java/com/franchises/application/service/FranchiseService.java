package com.franchises.application.service;

import com.franchises.application.port.in.FranchiseUseCase;
import com.franchises.application.port.out.FranchiseRepository;
import com.franchises.domain.exception.ConcurrencyConflictException;
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
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.UUID;
import java.util.function.BiFunction;

@Service
@RequiredArgsConstructor
public class FranchiseService implements FranchiseUseCase {

    /**
     * Ante una modificación concurrente se reintenta la operación completa: al
     * resuscribirse, la franquicia se relee con el estado y la versión más recientes y
     * la regla de negocio se reaplica sobre ese estado fresco.
     */
    private static final Retry CONCURRENCY_RETRY = Retry.backoff(3, Duration.ofMillis(50))
            .filter(ConcurrencyConflictException.class::isInstance)
            .onRetryExhaustedThrow((spec, signal) -> signal.failure());

    private final FranchiseRepository repository;

    @Override
    public Mono<Franchise> createFranchise(String name) {
        return repository.save(Franchise.builder().name(name).build());
    }

    @Override
    public Mono<Franchise> updateFranchiseName(String franchiseId, String newName) {
        return findFranchise(franchiseId)
                .map(franchise -> franchise.withName(newName))
                .flatMap(repository::save)
                .retryWhen(CONCURRENCY_RETRY);
    }

    @Override
    public Mono<Franchise> addBranch(String franchiseId, String branchName) {
        return findFranchise(franchiseId)
                .flatMap(franchise -> franchise.hasBranchNamed(branchName)
                        ? Mono.error(duplicateBranch(branchName))
                        : repository.save(franchise.addBranch(newBranch(branchName))))
                .retryWhen(CONCURRENCY_RETRY);
    }

    @Override
    public Mono<Franchise> updateBranchName(String franchiseId, String branchId, String newName) {
        return updateExistingBranch(franchiseId, branchId,
                (franchise, branch) -> franchise.hasBranchNamed(newName) && !branch.getName().equals(newName)
                        ? Mono.error(duplicateBranch(newName))
                        : Mono.just(branch.withName(newName)));
    }

    @Override
    public Mono<Franchise> addProduct(String franchiseId, String branchId, String productName, int stock) {
        return updateExistingBranch(franchiseId, branchId,
                (franchise, branch) -> branch.hasProductNamed(productName)
                        ? Mono.error(duplicateProduct(productName, branch.getName()))
                        : Mono.just(branch.addProduct(newProduct(productName, stock))));
    }

    @Override
    public Mono<Franchise> removeProduct(String franchiseId, String branchId, String productId) {
        return updateExistingBranch(franchiseId, branchId,
                (franchise, branch) -> branch.removeProductById(productId)
                        .map(Mono::just)
                        .orElseGet(() -> Mono.error(productNotFound(productId, branch.getName()))));
    }

    @Override
    public Mono<Franchise> updateProductStock(String franchiseId, String branchId, String productId, int stock) {
        return updateExistingBranch(franchiseId, branchId,
                (franchise, branch) -> branch.updateProductById(productId, product -> product.withStock(stock))
                        .map(Mono::just)
                        .orElseGet(() -> Mono.error(productNotFound(productId, branch.getName()))));
    }

    @Override
    public Mono<Franchise> updateProductName(String franchiseId, String branchId, String productId, String newName) {
        return updateExistingBranch(franchiseId, branchId,
                (franchise, branch) -> branch.findProductById(productId)
                        .map(current -> renameProduct(branch, current, newName))
                        .orElseGet(() -> Mono.error(productNotFound(productId, branch.getName()))));
    }

    @Override
    public Flux<TopStockProduct> topStockProducts(String franchiseId) {
        return findFranchise(franchiseId)
                .flatMapIterable(Franchise::topStockProducts);
    }

    private Mono<Branch> renameProduct(Branch branch, Product current, String newName) {
        if (branch.hasProductNamed(newName) && !current.getName().equals(newName)) {
            return Mono.error(duplicateProduct(newName, branch.getName()));
        }
        return Mono.just(branch.replaceProduct(current.withName(newName)));
    }

    private Branch newBranch(String name) {
        return Branch.builder().id(UUID.randomUUID().toString()).name(name).build();
    }

    private Product newProduct(String name, int stock) {
        return Product.builder().id(UUID.randomUUID().toString()).name(name).stock(stock).build();
    }

    private Mono<Franchise> findFranchise(String franchiseId) {
        return repository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new NotFoundException("Franquicia no encontrada: " + franchiseId)));
    }

    /**
     * Localiza la sucursal por id, aplica la operación de negocio y persiste el agregado
     * resultante.
     */
    private Mono<Franchise> updateExistingBranch(String franchiseId, String branchId,
            BiFunction<Franchise, Branch, Mono<Branch>> operation) {
        return findFranchise(franchiseId)
                .flatMap(franchise -> franchise.findBranchById(branchId)
                        .map(branch -> operation.apply(franchise, branch)
                                .map(updated -> franchise.replaceBranchById(branchId, updated))
                                .flatMap(repository::save))
                        .orElseGet(() -> Mono.error(branchNotFound(branchId))))
                .retryWhen(CONCURRENCY_RETRY);
    }

    private NotFoundException branchNotFound(String branchId) {
        return new NotFoundException("Sucursal no encontrada: " + branchId);
    }

    private NotFoundException productNotFound(String productId, String branchName) {
        return new NotFoundException(
                "Producto no encontrado: " + productId + " en la sucursal " + branchName);
    }

    private DuplicateResourceException duplicateBranch(String branchName) {
        return new DuplicateResourceException("Ya existe una sucursal con el nombre: " + branchName);
    }

    private DuplicateResourceException duplicateProduct(String productName, String branchName) {
        return new DuplicateResourceException(
                "Ya existe un producto con el nombre " + productName + " en la sucursal " + branchName);
    }
}
