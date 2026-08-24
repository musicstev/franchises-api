package com.franchises.infrastructure.adapter.out.mongodb;

import com.franchises.domain.exception.ConcurrencyConflictException;
import com.franchises.domain.model.Branch;
import com.franchises.domain.model.Franchise;
import com.franchises.domain.model.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de integración contra un MongoDB real (Testcontainers), no simulado. Verifica
 * que el mapeo del agregado y el bloqueo optimista ({@code @Version}) funcionan tal como
 * los ejercita Spring Data, algo que los tests unitarios con mocks no pueden garantizar.
 *
 * <p>Se ejecuta solo con {@code mvn verify} (fase integration-test, plugin Failsafe), no
 * con {@code mvn test}, para mantener rápido el ciclo de pruebas unitarias.
 */
@DataMongoTest
@Testcontainers
class FranchiseMongoAdapterIT {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
    }

    @Autowired
    private SpringDataFranchiseRepository springDataRepository;

    private FranchiseMongoAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new FranchiseMongoAdapter(springDataRepository);
    }

    @AfterEach
    void cleanUp() {
        springDataRepository.deleteAll().block();
    }

    @Test
    @DisplayName("guarda y recupera el agregado completo contra un MongoDB real")
    void savesAndReloadsTheFullAggregate() {
        Franchise franchise = Franchise.builder()
                .name("Mi Franquicia")
                .branches(List.of(Branch.builder()
                        .name("Centro")
                        .products(List.of(Product.builder().name("Café").stock(10).build()))
                        .build()))
                .build();

        Franchise saved = adapter.save(franchise).block();

        StepVerifier.create(adapter.findById(saved.getId()))
                .assertNext(reloaded -> {
                    assertThat(reloaded.getName()).isEqualTo("Mi Franquicia");
                    assertThat(reloaded.getVersion()).isNotNull();
                    assertThat(reloaded.findBranch("Centro").orElseThrow()
                            .findProduct("Café").orElseThrow().getStock()).isEqualTo(10);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("una escritura con versión desactualizada falla contra Mongo real")
    void staleVersionFailsAgainstRealMongo() {
        Franchise created = adapter.save(Franchise.builder().name("Concurrencia").build()).block();

        // Otra escritura exitosa incrementa la versión almacenada en Mongo.
        adapter.save(created.withName("Actualización A")).block();

        // 'created' aún referencia la versión original: Spring Data debe rechazarla.
        StepVerifier.create(adapter.save(created.withName("Actualización B")))
                .verifyError(ConcurrencyConflictException.class);
    }
}
