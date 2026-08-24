package com.franchises.application.service;

import com.franchises.application.port.out.FranchiseRepository;
import com.franchises.domain.exception.DuplicateResourceException;
import com.franchises.domain.exception.NotFoundException;
import com.franchises.domain.model.Branch;
import com.franchises.domain.model.Franchise;
import com.franchises.domain.model.Product;
import com.franchises.domain.model.TopStockProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FranchiseServiceTest {

    private static final String FRANCHISE_ID = "f-1";

    @Mock
    private FranchiseRepository repository;

    private FranchiseService service;

    private Franchise franchise;

    @BeforeEach
    void setUp() {
        service = new FranchiseService(repository);
        franchise = Franchise.builder()
                .id(FRANCHISE_ID)
                .name("Mi Franquicia")
                .branches(List.of(
                        Branch.builder()
                                .name("Centro")
                                .products(List.of(
                                        Product.builder().name("Café").stock(10).build(),
                                        Product.builder().name("Pan").stock(25).build()))
                                .build(),
                        Branch.builder().name("Norte").build()))
                .build();
        lenient().when(repository.save(any(Franchise.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    }

    private void givenFranchiseExists() {
        when(repository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));
    }

    private void givenFranchiseDoesNotExist() {
        when(repository.findById(FRANCHISE_ID)).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("createFranchise persiste una nueva franquicia con el nombre dado")
    void createFranchise() {
        StepVerifier.create(service.createFranchise("Nueva"))
                .assertNext(saved -> {
                    assertThat(saved.getName()).isEqualTo("Nueva");
                    assertThat(saved.getBranches()).isEmpty();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("updateFranchiseName renombra la franquicia")
    void updateFranchiseName() {
        givenFranchiseExists();

        StepVerifier.create(service.updateFranchiseName(FRANCHISE_ID, "Nueva Marca"))
                .assertNext(saved -> assertThat(saved.getName()).isEqualTo("Nueva Marca"))
                .verifyComplete();
    }

    @Test
    @DisplayName("updateFranchiseName falla cuando la franquicia no existe")
    void updateFranchiseNameNotFound() {
        givenFranchiseDoesNotExist();

        StepVerifier.create(service.updateFranchiseName(FRANCHISE_ID, "Nueva Marca"))
                .verifyError(NotFoundException.class);
    }

    @Test
    @DisplayName("addBranch agrega una sucursal nueva")
    void addBranch() {
        givenFranchiseExists();

        StepVerifier.create(service.addBranch(FRANCHISE_ID, "Sur"))
                .assertNext(saved -> assertThat(saved.hasBranch("Sur")).isTrue())
                .verifyComplete();
    }

    @Test
    @DisplayName("addBranch falla cuando la sucursal ya existe")
    void addBranchDuplicate() {
        givenFranchiseExists();

        StepVerifier.create(service.addBranch(FRANCHISE_ID, "Centro"))
                .verifyError(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("addBranch falla cuando la franquicia no existe")
    void addBranchFranchiseNotFound() {
        givenFranchiseDoesNotExist();

        StepVerifier.create(service.addBranch(FRANCHISE_ID, "Sur"))
                .verifyError(NotFoundException.class);
    }

    @Test
    @DisplayName("updateBranchName renombra la sucursal")
    void updateBranchName() {
        givenFranchiseExists();

        StepVerifier.create(service.updateBranchName(FRANCHISE_ID, "Centro", "Centro Histórico"))
                .assertNext(saved -> {
                    assertThat(saved.hasBranch("Centro Histórico")).isTrue();
                    assertThat(saved.hasBranch("Centro")).isFalse();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("updateBranchName permite renombrar con el mismo nombre")
    void updateBranchNameSameName() {
        givenFranchiseExists();

        StepVerifier.create(service.updateBranchName(FRANCHISE_ID, "Centro", "Centro"))
                .assertNext(saved -> assertThat(saved.hasBranch("Centro")).isTrue())
                .verifyComplete();
    }

    @Test
    @DisplayName("updateBranchName falla cuando el nuevo nombre ya existe")
    void updateBranchNameDuplicate() {
        givenFranchiseExists();

        StepVerifier.create(service.updateBranchName(FRANCHISE_ID, "Centro", "Norte"))
                .verifyError(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("updateBranchName falla cuando la sucursal no existe")
    void updateBranchNameNotFound() {
        givenFranchiseExists();

        StepVerifier.create(service.updateBranchName(FRANCHISE_ID, "Sur", "Sur 2"))
                .verifyError(NotFoundException.class);
    }

    @Test
    @DisplayName("addProduct agrega un producto a la sucursal")
    void addProduct() {
        givenFranchiseExists();

        StepVerifier.create(service.addProduct(FRANCHISE_ID, "Centro", "Leche", 5))
                .assertNext(saved -> {
                    Product added = saved.findBranch("Centro").orElseThrow()
                            .findProduct("Leche").orElseThrow();
                    assertThat(added.getStock()).isEqualTo(5);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("addProduct falla cuando el producto ya existe en la sucursal")
    void addProductDuplicate() {
        givenFranchiseExists();

        StepVerifier.create(service.addProduct(FRANCHISE_ID, "Centro", "Café", 5))
                .verifyError(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("addProduct falla cuando la sucursal no existe")
    void addProductBranchNotFound() {
        givenFranchiseExists();

        StepVerifier.create(service.addProduct(FRANCHISE_ID, "Sur", "Leche", 5))
                .verifyError(NotFoundException.class);
    }

    @Test
    @DisplayName("removeProduct elimina el producto de la sucursal")
    void removeProduct() {
        givenFranchiseExists();

        StepVerifier.create(service.removeProduct(FRANCHISE_ID, "Centro", "Café"))
                .assertNext(saved -> assertThat(
                        saved.findBranch("Centro").orElseThrow().hasProduct("Café")).isFalse())
                .verifyComplete();
    }

    @Test
    @DisplayName("removeProduct falla cuando el producto no existe")
    void removeProductNotFound() {
        givenFranchiseExists();

        StepVerifier.create(service.removeProduct(FRANCHISE_ID, "Centro", "Leche"))
                .verifyError(NotFoundException.class);
    }

    @Test
    @DisplayName("updateProductStock modifica el stock del producto")
    void updateProductStock() {
        givenFranchiseExists();

        StepVerifier.create(service.updateProductStock(FRANCHISE_ID, "Centro", "Café", 42))
                .assertNext(saved -> assertThat(saved.findBranch("Centro").orElseThrow()
                        .findProduct("Café").orElseThrow().getStock()).isEqualTo(42))
                .verifyComplete();
    }

    @Test
    @DisplayName("updateProductStock falla cuando el producto no existe")
    void updateProductStockNotFound() {
        givenFranchiseExists();

        StepVerifier.create(service.updateProductStock(FRANCHISE_ID, "Centro", "Leche", 42))
                .verifyError(NotFoundException.class);
    }

    @Test
    @DisplayName("updateProductName renombra el producto")
    void updateProductName() {
        givenFranchiseExists();

        StepVerifier.create(service.updateProductName(FRANCHISE_ID, "Centro", "Café", "Café Premium"))
                .assertNext(saved -> {
                    Branch branch = saved.findBranch("Centro").orElseThrow();
                    assertThat(branch.hasProduct("Café Premium")).isTrue();
                    assertThat(branch.hasProduct("Café")).isFalse();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("updateProductName permite renombrar con el mismo nombre")
    void updateProductNameSameName() {
        givenFranchiseExists();

        StepVerifier.create(service.updateProductName(FRANCHISE_ID, "Centro", "Café", "Café"))
                .assertNext(saved -> assertThat(
                        saved.findBranch("Centro").orElseThrow().hasProduct("Café")).isTrue())
                .verifyComplete();
    }

    @Test
    @DisplayName("updateProductName falla cuando el nuevo nombre ya existe en la sucursal")
    void updateProductNameDuplicate() {
        givenFranchiseExists();

        StepVerifier.create(service.updateProductName(FRANCHISE_ID, "Centro", "Café", "Pan"))
                .verifyError(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("updateProductName falla cuando el producto no existe")
    void updateProductNameNotFound() {
        givenFranchiseExists();

        StepVerifier.create(service.updateProductName(FRANCHISE_ID, "Centro", "Leche", "Leche 2"))
                .verifyError(NotFoundException.class);
    }

    @Test
    @DisplayName("topStockProducts devuelve el producto de mayor stock por sucursal")
    void topStockProducts() {
        givenFranchiseExists();

        StepVerifier.create(service.topStockProducts(FRANCHISE_ID))
                .expectNext(new TopStockProduct("Centro", "Pan", 25))
                .verifyComplete();
    }

    @Test
    @DisplayName("topStockProducts falla cuando la franquicia no existe")
    void topStockProductsNotFound() {
        givenFranchiseDoesNotExist();

        StepVerifier.create(service.topStockProducts(FRANCHISE_ID))
                .verifyError(NotFoundException.class);
    }
}
