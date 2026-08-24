package com.franchises.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

/**
 * Producto ofertado en una sucursal. Inmutable: toda modificación produce una nueva instancia.
 *
 * <p>{@code id} es la identidad estable del producto (generada por el servidor al crearlo);
 * {@code name} es un atributo de negocio mutable, no una clave.
 */
@Value
@Builder(toBuilder = true)
public class Product {

    String id;

    @With
    String name;

    @With
    int stock;
}
