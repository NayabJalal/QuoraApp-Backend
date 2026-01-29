package com.quoraBackend.repositories;

import com.quoraBackend.models.Question;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.util.MongoCompatibilityAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface QuestionRepo extends ReactiveMongoRepository<Question , String> {
    Flux<Question> findByAuthorId(String authorId);

    Mono<Long> countByAuthorId(String authorId);
}
