package com.franchises.domain.model;

/**
 * Proyección de consulta: producto con mayor stock de una sucursal.
 */
public record TopStockProduct(String branchName, String productName, int stock) {
}
