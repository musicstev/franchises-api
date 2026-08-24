package com.franchises.infrastructure.adapter.out.mongodb;

import com.franchises.domain.model.Branch;
import com.franchises.domain.model.Franchise;
import com.franchises.domain.model.Product;
import com.franchises.infrastructure.adapter.out.mongodb.document.FranchiseDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FranchiseDocumentMapperTest {

    @Test
    @DisplayName("mapea dominio a documento y de vuelta sin pérdida de información")
    void roundTrip() {
        Franchise franchise = Franchise.builder()
                .id("f-1")
                .name("Mi Franquicia")
                .branches(List.of(
                        Branch.builder()
                                .id("b-1")
                                .name("Centro")
                                .products(List.of(Product.builder().id("p-1").name("Café").stock(10).build()))
                                .build(),
                        Branch.builder().id("b-2").name("Vacía").build()))
                .build();

        Franchise result = FranchiseDocumentMapper.toDomain(FranchiseDocumentMapper.toDocument(franchise));

        assertThat(result).isEqualTo(franchise);
    }

    @Test
    @DisplayName("toDomain tolera listas nulas en documentos persistidos")
    void toDomainWithNullLists() {
        FranchiseDocument document = FranchiseDocument.builder()
                .id("f-2")
                .name("Sin Sucursales")
                .build();

        Franchise result = FranchiseDocumentMapper.toDomain(document);

        assertThat(result.getBranches()).isEmpty();
    }

    @Test
    @DisplayName("toDomain tolera productos nulos en una sucursal persistida")
    void toDomainWithNullProducts() {
        FranchiseDocument document = FranchiseDocument.builder()
                .id("f-3")
                .name("Con Sucursal")
                .branches(List.of(
                        com.franchises.infrastructure.adapter.out.mongodb.document.BranchDocument.builder()
                                .id("b-1")
                                .name("Centro")
                                .build()))
                .build();

        Franchise result = FranchiseDocumentMapper.toDomain(document);

        assertThat(result.findBranchById("b-1").orElseThrow().getProducts()).isEmpty();
    }
}
