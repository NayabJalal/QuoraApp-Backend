package com.quoraBackend.repositories;

import com.quoraBackend.models.Questions;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface QuestionRepo extends ReactiveMongoRepository<Questions , String> {
    Mono<Questions> findById(String id);

    Flux<Questions> findAll();

    Mono<Void> deleteById(String id);

}
