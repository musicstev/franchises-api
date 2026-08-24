package com.franchises.application.port.out;

import com.franchises.domain.model.Franchise;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida: persistencia del agregado franquicia.
 */
public interface FranchiseRepository {

    Mono<Franchise> save(Franchise franchise);

    Mono<Franchise> findById(String id);
}
