package com.franchises.infrastructure.adapter.in.web;

import com.franchises.domain.model.Branch;
import com.franchises.domain.model.Franchise;
import com.franchises.domain.model.Product;
import com.franchises.domain.model.TopStockProduct;
import com.franchises.infrastructure.adapter.in.web.dto.BranchResponse;
import com.franchises.infrastructure.adapter.in.web.dto.FranchiseResponse;
import com.franchises.infrastructure.adapter.in.web.dto.ProductResponse;
import com.franchises.infrastructure.adapter.in.web.dto.TopStockProductResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FranchiseDtoMapper {

    public FranchiseResponse toResponse(Franchise franchise) {
        return new FranchiseResponse(
                franchise.getId(),
                franchise.getName(),
                franchise.getBranches().stream()
                        .map(FranchiseDtoMapper::toResponse)
                        .toList());
    }

    public TopStockProductResponse toResponse(TopStockProduct topStockProduct) {
        return new TopStockProductResponse(
                topStockProduct.branchName(),
                topStockProduct.productName(),
                topStockProduct.stock());
    }

    private BranchResponse toResponse(Branch branch) {
        return new BranchResponse(
                branch.getId(),
                branch.getName(),
                branch.getProducts().stream()
                        .map(FranchiseDtoMapper::toResponse)
                        .toList());
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getStock());
    }
}
