package com.franchises.infrastructure.adapter.out.mongodb;

import com.franchises.domain.exception.ConcurrencyConflictException;
import com.franchises.domain.model.Branch;
import com.franchises.domain.model.Franchise;
import com.franchises.domain.model.Product;
import com.franchises.infrastructure.adapter.out.mongodb.document.FranchiseDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FranchiseMongoAdapterTest {

    @Mock
    private SpringDataFranchiseRepository springDataRepository;

    private FranchiseMongoAdapter adapter;

    private final Franchise franchise = Franchise.builder()
            .id("f-1")
            .name("Mi Franquicia")
            .branches(List.of(Branch.builder()
                    .id("b-1")
                    .name("Centro")
                    .products(List.of(Product.builder().id("p-1").name("Café").stock(10).build()))
                    .build()))
            .build();

    @BeforeEach
    void setUp() {
        adapter = new FranchiseMongoAdapter(springDataRepository);
    }

    @Test
    @DisplayName("save persiste el documento y devuelve el dominio mapeado")
    void save() {
        when(springDataRepository.save(any(FranchiseDocument.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(adapter.save(franchise))
                .expectNext(franchise)
                .verifyComplete();
    }

    @Test
    @DisplayName("findById devuelve el dominio mapeado cuando existe")
    void findById() {
        when(springDataRepository.findById("f-1"))
                .thenReturn(Mono.just(FranchiseDocumentMapper.toDocument(franchise)));

        StepVerifier.create(adapter.findById("f-1"))
                .expectNext(franchise)
                .verifyComplete();
    }

    @Test
    @DisplayName("findById devuelve vacío cuando no existe")
    void findByIdEmpty() {
        when(springDataRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById("missing"))
                .verifyComplete();
    }

    @Test
    @DisplayName("traduce el fallo de bloqueo optimista de Spring Data a una excepción de dominio")
    void translatesOptimisticLockingFailure() {
        when(springDataRepository.save(any(FranchiseDocument.class)))
                .thenReturn(Mono.error(new OptimisticLockingFailureException("version mismatch")));

        StepVerifier.create(adapter.save(franchise))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ConcurrencyConflictException.class);
                    assertThat(error.getMessage()).contains("f-1");
                    assertThat(error.getCause()).isInstanceOf(OptimisticLockingFailureException.class);
                })
                .verify();
    }

    @Test
    @DisplayName("otros errores de persistencia no se traducen")
    void doesNotTranslateOtherErrors() {
        when(springDataRepository.save(any(FranchiseDocument.class)))
                .thenReturn(Mono.error(new IllegalStateException("boom")));

        StepVerifier.create(adapter.save(franchise))
                .verifyError(IllegalStateException.class);
    }
}
