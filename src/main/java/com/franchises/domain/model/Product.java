package com.franchises.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

/**
 * Producto ofertado en una sucursal. Inmutable: toda modificación produce una nueva instancia.
 */
@Value
@Builder(toBuilder = true)
public class Product {

    @With
    String name;

    @With
    int stock;
}
