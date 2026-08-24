package com.franchises.infrastructure.adapter.in.web.dto;

import java.util.List;

public record BranchResponse(String name, List<ProductResponse> products) {
}
