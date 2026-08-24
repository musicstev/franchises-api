package com.franchises.infrastructure.adapter.out.mongodb;

import com.franchises.application.port.out.FranchiseRepository;
import com.franchises.domain.model.Franchise;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class FranchiseMongoAdapter implements FranchiseRepository {

    private final SpringDataFranchiseRepository repository;

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        return repository.save(FranchiseDocumentMapper.toDocument(franchise))
                .map(FranchiseDocumentMapper::toDomain);
    }

    @Override
    public Mono<Franchise> findById(String id) {
        return repository.findById(id)
                .map(FranchiseDocumentMapper::toDomain);
    }
}
