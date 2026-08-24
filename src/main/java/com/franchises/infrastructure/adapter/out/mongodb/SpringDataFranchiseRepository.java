package com.franchises.infrastructure.adapter.out.mongodb;

import com.franchises.infrastructure.adapter.out.mongodb.document.FranchiseDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface SpringDataFranchiseRepository extends ReactiveMongoRepository<FranchiseDocument, String> {
}
