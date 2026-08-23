package com.quoraBackend.repositories;

import com.quoraBackend.models.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CommentRepo extends ReactiveMongoRepository<Comment, String> {
    Flux<Comment> findByTargetIdAndTargetTypeAndParentIdIsNullOrderByCreatedAtDesc(
            String targetId,
            String targetType,
            Pageable pageable
    );
    Flux<Comment> findByParentIdOrderByCreatedAtAsc(String parentId, Pageable pageable);
    Mono<Long> countByParentId(String parentId);
    Mono<Void> deleteByTargetId(String targetId);
    Mono<Void> deleteByTargetIdIn(List<String> targetIds);
    Mono<Void> deleteByParentId(String parentId);
}