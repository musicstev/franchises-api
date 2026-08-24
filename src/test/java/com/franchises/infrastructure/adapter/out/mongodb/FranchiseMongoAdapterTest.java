package com.franchises.infrastructure.adapter.out.mongodb;

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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

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
                    .name("Centro")
                    .products(List.of(Product.builder().name("Café").stock(10).build()))
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
}
