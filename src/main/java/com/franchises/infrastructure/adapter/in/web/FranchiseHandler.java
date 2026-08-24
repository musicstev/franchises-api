package com.franchises.infrastructure.adapter.in.web;

import com.franchises.application.port.in.FranchiseUseCase;
import com.franchises.domain.model.Franchise;
import com.franchises.infrastructure.adapter.in.web.dto.BranchRequest;
import com.franchises.infrastructure.adapter.in.web.dto.FranchiseRequest;
import com.franchises.infrastructure.adapter.in.web.dto.NameUpdateRequest;
import com.franchises.infrastructure.adapter.in.web.dto.ProductRequest;
import com.franchises.infrastructure.adapter.in.web.dto.StockUpdateRequest;
import com.franchises.infrastructure.adapter.in.web.dto.TopStockProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class FranchiseHandler {

    private final FranchiseUseCase franchiseUseCase;
    private final RequestValidator validator;

    public Mono<ServerResponse> createFranchise(ServerRequest request) {
        return body(request, FranchiseRequest.class)
                .flatMap(dto -> franchiseUseCase.createFranchise(dto.name()))
                .flatMap(franchise -> respond(HttpStatus.CREATED, franchise));
    }

    public Mono<ServerResponse> updateFranchiseName(ServerRequest request) {
        return body(request, NameUpdateRequest.class)
                .flatMap(dto -> franchiseUseCase.updateFranchiseName(franchiseId(request), dto.name()))
                .flatMap(franchise -> respond(HttpStatus.OK, franchise));
    }

    public Mono<ServerResponse> addBranch(ServerRequest request) {
        return body(request, BranchRequest.class)
                .flatMap(dto -> franchiseUseCase.addBranch(franchiseId(request), dto.name()))
                .flatMap(franchise -> respond(HttpStatus.CREATED, franchise));
    }

    public Mono<ServerResponse> updateBranchName(ServerRequest request) {
        return body(request, NameUpdateRequest.class)
                .flatMap(dto -> franchiseUseCase.updateBranchName(
                        franchiseId(request), branchName(request), dto.name()))
                .flatMap(franchise -> respond(HttpStatus.OK, franchise));
    }

    public Mono<ServerResponse> addProduct(ServerRequest request) {
        return body(request, ProductRequest.class)
                .flatMap(dto -> franchiseUseCase.addProduct(
                        franchiseId(request), branchName(request), dto.name(), dto.stock()))
                .flatMap(franchise -> respond(HttpStatus.CREATED, franchise));
    }

    public Mono<ServerResponse> removeProduct(ServerRequest request) {
        return franchiseUseCase
                .removeProduct(franchiseId(request), branchName(request), productName(request))
                .flatMap(franchise -> respond(HttpStatus.OK, franchise));
    }

    public Mono<ServerResponse> updateProductStock(ServerRequest request) {
        return body(request, StockUpdateRequest.class)
                .flatMap(dto -> franchiseUseCase.updateProductStock(
                        franchiseId(request), branchName(request), productName(request), dto.stock()))
                .flatMap(franchise -> respond(HttpStatus.OK, franchise));
    }

    public Mono<ServerResponse> updateProductName(ServerRequest request) {
        return body(request, NameUpdateRequest.class)
                .flatMap(dto -> franchiseUseCase.updateProductName(
                        franchiseId(request), branchName(request), productName(request), dto.name()))
                .flatMap(franchise -> respond(HttpStatus.OK, franchise));
    }

    public Mono<ServerResponse> topStockProducts(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(franchiseUseCase.topStockProducts(franchiseId(request))
                                .map(FranchiseDtoMapper::toResponse),
                        TopStockProductResponse.class);
    }

    private <T> Mono<T> body(ServerRequest request, Class<T> bodyType) {
        return request.bodyToMono(bodyType)
                .switchIfEmpty(Mono.error(new ServerWebInputException("El cuerpo de la petición es obligatorio")))
                .flatMap(validator::validate);
    }

    private Mono<ServerResponse> respond(HttpStatus status, Franchise franchise) {
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(FranchiseDtoMapper.toResponse(franchise));
    }

    private String franchiseId(ServerRequest request) {
        return request.pathVariable("franchiseId");
    }

    private String branchName(ServerRequest request) {
        return request.pathVariable("branchName");
    }

    private String productName(ServerRequest request) {
        return request.pathVariable("productName");
    }
}
