package com.quoraBackend.repositories;

import com.quoraBackend.models.Answer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AnswersRepo extends ReactiveMongoRepository<Answer, String> {
    Flux<Answer> findByQuestionId(String questionId);
    Mono<Answer> findByIdAndQuestionId(String id, String questionId);
    Mono<Void> deleteByQuestionId(String questionId);
}
