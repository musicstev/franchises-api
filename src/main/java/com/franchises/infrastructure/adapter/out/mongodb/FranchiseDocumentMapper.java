package com.franchises.infrastructure.adapter.out.mongodb;

import com.franchises.domain.model.Branch;
import com.franchises.domain.model.Franchise;
import com.franchises.domain.model.Product;
import com.franchises.infrastructure.adapter.out.mongodb.document.BranchDocument;
import com.franchises.infrastructure.adapter.out.mongodb.document.FranchiseDocument;
import com.franchises.infrastructure.adapter.out.mongodb.document.ProductDocument;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Optional;

@UtilityClass
public class FranchiseDocumentMapper {

    public FranchiseDocument toDocument(Franchise franchise) {
        return FranchiseDocument.builder()
                .id(franchise.getId())
                .name(franchise.getName())
                .branches(franchise.getBranches().stream()
                        .map(FranchiseDocumentMapper::toDocument)
                        .toList())
                .build();
    }

    public Franchise toDomain(FranchiseDocument document) {
        return Franchise.builder()
                .id(document.getId())
                .name(document.getName())
                .branches(Optional.ofNullable(document.getBranches()).orElse(List.of()).stream()
                        .map(FranchiseDocumentMapper::toDomain)
                        .toList())
                .build();
    }

    private BranchDocument toDocument(Branch branch) {
        return BranchDocument.builder()
                .name(branch.getName())
                .products(branch.getProducts().stream()
                        .map(product -> ProductDocument.builder()
                                .name(product.getName())
                                .stock(product.getStock())
                                .build())
                        .toList())
                .build();
    }

    private Branch toDomain(BranchDocument document) {
        return Branch.builder()
                .name(document.getName())
                .products(Optional.ofNullable(document.getProducts()).orElse(List.of()).stream()
                        .map(product -> Product.builder()
                                .name(product.getName())
                                .stock(product.getStock())
                                .build())
                        .toList())
                .build();
    }
}
