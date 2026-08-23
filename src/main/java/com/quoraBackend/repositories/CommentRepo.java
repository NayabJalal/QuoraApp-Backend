package com.quoraBackend.repositories;

import com.quoraBackend.models.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface CommentRepo extends ReactiveMongoRepository<Comment, String> {
    Flux<Comment> findByTargetIdAndTargetTypeOrderByCreatedAtDesc(String targetId, String targetType, Pageable pageable);
    Flux<Comment> findByParentIdOrderByCreatedAtAsc(String parentId, Pageable pageable);
}