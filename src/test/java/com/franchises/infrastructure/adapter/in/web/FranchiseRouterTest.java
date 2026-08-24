package com.franchises.infrastructure.adapter.in.web;

import com.franchises.application.port.in.FranchiseUseCase;
import com.franchises.domain.exception.DuplicateResourceException;
import com.franchises.domain.exception.NotFoundException;
import com.franchises.domain.model.Branch;
import com.franchises.domain.model.Franchise;
import com.franchises.domain.model.Product;
import com.franchises.domain.model.TopStockProduct;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest
@Import({FranchiseRouter.class, FranchiseHandler.class, RequestValidator.class, GlobalErrorHandler.class})
class FranchiseRouterTest {

    private static final String FRANCHISE_ID = "f-1";
    private static final String BRANCH_ID = "b-centro";
    private static final String PRODUCT_ID = "p-cafe";

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private FranchiseUseCase franchiseUseCase;

    private final Franchise franchise = Franchise.builder()
            .id(FRANCHISE_ID)
            .name("Mi Franquicia")
            .branches(List.of(Branch.builder()
                    .id(BRANCH_ID)
                    .name("Centro")
                    .products(List.of(Product.builder().id(PRODUCT_ID).name("Café").stock(10).build()))
                    .build()))
            .build();

    @Test
    @DisplayName("POST /api/franchises crea la franquicia y responde 201")
    void createFranchise() {
        when(franchiseUseCase.createFranchise("Mi Franquicia")).thenReturn(Mono.just(franchise));

        webTestClient.post().uri("/api/franchises")
                .bodyValue("{\"name\":\"Mi Franquicia\"}")
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(FRANCHISE_ID)
                .jsonPath("$.name").isEqualTo("Mi Franquicia")
                .jsonPath("$.branches[0].id").isEqualTo(BRANCH_ID)
                .jsonPath("$.branches[0].name").isEqualTo("Centro")
                .jsonPath("$.branches[0].products[0].id").isEqualTo(PRODUCT_ID)
                .jsonPath("$.branches[0].products[0].stock").isEqualTo(10);
    }

    @Test
    @DisplayName("POST /api/franchises responde 400 cuando el nombre está en blanco")
    void createFranchiseInvalid() {
        webTestClient.post().uri("/api/franchises")
                .header("Content-Type", "application/json")
                .bodyValue("{\"name\":\"  \"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("el nombre de la franquicia es obligatorio");
    }

    @Test
    @DisplayName("POST /api/franchises responde 400 cuando no hay cuerpo")
    void createFranchiseWithoutBody() {
        webTestClient.post().uri("/api/franchises")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("PATCH /api/franchises/{id}/name actualiza el nombre de la franquicia")
    void updateFranchiseName() {
        when(franchiseUseCase.updateFranchiseName(FRANCHISE_ID, "Nueva Marca"))
                .thenReturn(Mono.just(franchise.withName("Nueva Marca")));

        webTestClient.patch().uri("/api/franchises/{id}/name", FRANCHISE_ID)
                .header("Content-Type", "application/json")
                .bodyValue("{\"name\":\"Nueva Marca\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Nueva Marca");
    }

    @Test
    @DisplayName("PATCH /api/franchises/{id}/name responde 404 cuando la franquicia no existe")
    void updateFranchiseNameNotFound() {
        when(franchiseUseCase.updateFranchiseName(anyString(), anyString()))
                .thenReturn(Mono.error(new NotFoundException("Franquicia no encontrada: f-9")));

        webTestClient.patch().uri("/api/franchises/f-9/name")
                .header("Content-Type", "application/json")
                .bodyValue("{\"name\":\"Nueva\"}")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Franquicia no encontrada: f-9");
    }

    @Test
    @DisplayName("POST /api/franchises/{id}/branches agrega una sucursal y responde 201")
    void addBranch() {
        when(franchiseUseCase.addBranch(FRANCHISE_ID, "Norte")).thenReturn(Mono.just(franchise));

        webTestClient.post().uri("/api/franchises/{id}/branches", FRANCHISE_ID)
                .header("Content-Type", "application/json")
                .bodyValue("{\"name\":\"Norte\"}")
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    @DisplayName("POST /api/franchises/{id}/branches recorta espacios del nombre antes de delegarlo")
    void addBranchTrimsName() {
        when(franchiseUseCase.addBranch(anyString(), anyString())).thenReturn(Mono.just(franchise));

        webTestClient.post().uri("/api/franchises/{id}/branches", FRANCHISE_ID)
                .header("Content-Type", "application/json")
                .bodyValue("{\"name\":\"  Norte  \"}")
                .exchange()
                .expectStatus().isCreated();

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        verify(franchiseUseCase).addBranch(eq(FRANCHISE_ID), nameCaptor.capture());
        assertThat(nameCaptor.getValue()).isEqualTo("Norte");
    }

    @Test
    @DisplayName("POST /api/franchises/{id}/branches responde 409 cuando la sucursal ya existe")
    void addBranchDuplicate() {
        when(franchiseUseCase.addBranch(anyString(), anyString()))
                .thenReturn(Mono.error(new DuplicateResourceException("Ya existe una sucursal con el nombre: Centro")));

        webTestClient.post().uri("/api/franchises/{id}/branches", FRANCHISE_ID)
                .header("Content-Type", "application/json")
                .bodyValue("{\"name\":\"Centro\"}")
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Ya existe una sucursal con el nombre: Centro");
    }

    @Test
    @DisplayName("PATCH /api/franchises/{id}/branches/{branchId}/name renombra la sucursal")
    void updateBranchName() {
        when(franchiseUseCase.updateBranchName(FRANCHISE_ID, BRANCH_ID, "Centro Histórico"))
                .thenReturn(Mono.just(franchise));

        webTestClient.patch().uri("/api/franchises/{id}/branches/{branchId}/name", FRANCHISE_ID, BRANCH_ID)
                .header("Content-Type", "application/json")
                .bodyValue("{\"name\":\"Centro Histórico\"}")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("POST .../branches/{branchId}/products agrega un producto y responde 201")
    void addProduct() {
        when(franchiseUseCase.addProduct(FRANCHISE_ID, BRANCH_ID, "Leche", 5)).thenReturn(Mono.just(franchise));

        webTestClient.post().uri("/api/franchises/{id}/branches/{branchId}/products", FRANCHISE_ID, BRANCH_ID)
                .header("Content-Type", "application/json")
                .bodyValue("{\"name\":\"Leche\",\"stock\":5}")
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    @DisplayName("POST .../products responde 400 con stock negativo")
    void addProductNegativeStock() {
        webTestClient.post().uri("/api/franchises/{id}/branches/{branchId}/products", FRANCHISE_ID, BRANCH_ID)
                .header("Content-Type", "application/json")
                .bodyValue("{\"name\":\"Leche\",\"stock\":-1}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("el stock no puede ser negativo");
    }

    @Test
    @DisplayName("DELETE .../products/{productId} elimina el producto")
    void removeProduct() {
        when(franchiseUseCase.removeProduct(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID)).thenReturn(Mono.just(franchise));

        webTestClient.delete()
                .uri("/api/franchises/{id}/branches/{branchId}/products/{productId}",
                        FRANCHISE_ID, BRANCH_ID, PRODUCT_ID)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("PATCH .../products/{productId}/stock modifica el stock")
    void updateProductStock() {
        when(franchiseUseCase.updateProductStock(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, 42))
                .thenReturn(Mono.just(franchise));

        webTestClient.patch()
                .uri("/api/franchises/{id}/branches/{branchId}/products/{productId}/stock",
                        FRANCHISE_ID, BRANCH_ID, PRODUCT_ID)
                .header("Content-Type", "application/json")
                .bodyValue("{\"stock\":42}")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("PATCH .../products/{productId}/stock responde 400 sin stock")
    void updateProductStockMissing() {
        webTestClient.patch()
                .uri("/api/franchises/{id}/branches/{branchId}/products/{productId}/stock",
                        FRANCHISE_ID, BRANCH_ID, PRODUCT_ID)
                .header("Content-Type", "application/json")
                .bodyValue("{}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("el stock es obligatorio");
    }

    @Test
    @DisplayName("PATCH .../products/{productId}/name renombra el producto")
    void updateProductName() {
        when(franchiseUseCase.updateProductName(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, "Café Premium"))
                .thenReturn(Mono.just(franchise));

        webTestClient.patch()
                .uri("/api/franchises/{id}/branches/{branchId}/products/{productId}/name",
                        FRANCHISE_ID, BRANCH_ID, PRODUCT_ID)
                .header("Content-Type", "application/json")
                .bodyValue("{\"name\":\"Café Premium\"}")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("GET /api/franchises/{id}/top-stock-products lista el producto de mayor stock por sucursal")
    void topStockProducts() {
        when(franchiseUseCase.topStockProducts(FRANCHISE_ID)).thenReturn(Flux.just(
                new TopStockProduct("Centro", "Pan", 25),
                new TopStockProduct("Norte", "Leche", 7)));

        webTestClient.get().uri("/api/franchises/{id}/top-stock-products", FRANCHISE_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].branchName").isEqualTo("Centro")
                .jsonPath("$[0].productName").isEqualTo("Pan")
                .jsonPath("$[0].stock").isEqualTo(25)
                .jsonPath("$[1].branchName").isEqualTo("Norte");
    }

    @Test
    @DisplayName("stock update con valor negativo responde 400")
    void updateProductStockNegative() {
        webTestClient.patch()
                .uri("/api/franchises/{id}/branches/{branchId}/products/{productId}/stock",
                        FRANCHISE_ID, BRANCH_ID, PRODUCT_ID)
                .header("Content-Type", "application/json")
                .bodyValue("{\"stock\":-10}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("PATCH .../products/{productId}/stock acepta stock 0")
    void updateProductStockZero() {
        when(franchiseUseCase.updateProductStock(anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(Mono.just(franchise));

        webTestClient.patch()
                .uri("/api/franchises/{id}/branches/{branchId}/products/{productId}/stock",
                        FRANCHISE_ID, BRANCH_ID, PRODUCT_ID)
                .header("Content-Type", "application/json")
                .bodyValue("{\"stock\":0}")
                .exchange()
                .expectStatus().isOk();
    }
}
