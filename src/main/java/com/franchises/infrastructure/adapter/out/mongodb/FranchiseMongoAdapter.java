package com.franchises.infrastructure.adapter.out.mongodb;

import com.franchises.application.port.out.FranchiseRepository;
import com.franchises.domain.exception.ConcurrencyConflictException;
import com.franchises.domain.model.Franchise;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class FranchiseMongoAdapter implements FranchiseRepository {

    private final SpringDataFranchiseRepository repository;

    /**
     * Persiste el agregado. El bloqueo optimista de Spring Data convierte cada escritura
     * en un compare-and-swap sobre la versión del documento; el fallo tecnológico se
     * traduce aquí a una excepción de dominio para no filtrar Spring Data hacia arriba.
     */
    @Override
    public Mono<Franchise> save(Franchise franchise) {
        return repository.save(FranchiseDocumentMapper.toDocument(franchise))
                .map(FranchiseDocumentMapper::toDomain)
                .onErrorMap(OptimisticLockingFailureException.class,
                        ex -> new ConcurrencyConflictException(
                                "La franquicia fue modificada concurrentemente: " + franchise.getId(), ex));
    }

    @Override
    public Mono<Franchise> findById(String id) {
        return repository.findById(id)
                .map(FranchiseDocumentMapper::toDomain);
    }
}
