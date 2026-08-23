package com.quoraBackend.repositories;

import com.quoraBackend.models.Like;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface LikeRepo extends ReactiveMongoRepository<Like, String> {

    Mono<Long> countByTargetIdAndTargetTypeAndLiked(
            String targetId,
            String targetType,
            Boolean liked
    );

    Mono<Like> findByUserIdAndTargetIdAndTargetType(
            String userId,
            String targetId,
            String targetType
    );
    // Bulk delete likes for target IDs
    Mono<Void> deleteByTargetId(String targetId);
    Mono<Void> deleteByTargetIdIn(List<String> targetIds);
}