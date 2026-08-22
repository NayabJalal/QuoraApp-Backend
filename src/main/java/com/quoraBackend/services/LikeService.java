package com.quoraBackend.services;

import com.quoraBackend.adapter.LikeAdapter;
import com.quoraBackend.dto.LikeRequestDTO;
import com.quoraBackend.dto.LikeResponseDTO;
import com.quoraBackend.models.Like;
import com.quoraBackend.repositories.LikeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class LikeService implements ILikeService {

    private final LikeRepo likeRepo;

    @Override
    public Mono<LikeResponseDTO> createLike(LikeRequestDTO dto) {
        return likeRepo.findByUserIdAndTargetIdAndTargetType(dto.getUserId(), dto.getTargetId(), dto.getTargetType())
                .defaultIfEmpty(LikeAdapter.toLike(dto))
                .flatMap(like -> {
                    if (like.getId() != null && like.getLiked().equals(dto.getLiked())) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.CONFLICT, "You have already voted this way."
                        ));
                    }
                    like.setLiked(dto.getLiked());
                    return likeRepo.save(like);
                })
                .map(LikeAdapter::toLikeResponseDTO);
    }

    @Override
    public Mono<LikeResponseDTO> countLikesByTargetIdAndTargetType(String targetId, String targetType) {
        return likeRepo.countByTargetIdAndTargetTypeAndLiked(targetId, targetType, true)
                .map(count -> LikeResponseDTO.builder()
                        .targetId(targetId)
                        .targetType(targetType)
                        .liked(true)
                        .count(count)
                        .build());
    }

    @Override
    public Mono<LikeResponseDTO> countDisLikesByTargetIdAndTargetType(String targetId, String targetType) {
        return likeRepo.countByTargetIdAndTargetTypeAndLiked(targetId, targetType, false)
                .map(count -> LikeResponseDTO.builder()
                        .targetId(targetId)
                        .targetType(targetType)
                        .liked(false)
                        .count(count)
                        .build());
    }
    @Override
    public Mono<LikeResponseDTO> toggleLike(String userId, String targetId, String targetType, Boolean liked) {
        return likeRepo.findByUserIdAndTargetIdAndTargetType(userId, targetId, targetType)
                .defaultIfEmpty(Like.builder().userId(userId).targetId(targetId).targetType(targetType).build())
                .flatMap(existing -> {
                    // 1. New reaction: save to DB
                    if (existing.getId() == null) {
                        existing.setLiked(liked);
                        return likeRepo.save(existing).map(LikeAdapter::toLikeResponseDTO);
                    }
                    // 2. Clicked same reaction again: remove reaction from DB
                    if (existing.getLiked().equals(liked)) {
                        return likeRepo.delete(existing)
                                .then(Mono.just(LikeResponseDTO.builder()
                                        .userId(userId)
                                        .targetId(targetId)
                                        .targetType(targetType)
                                        .liked(null)
                                        .build()));
                    }
                    // 3. Clicked opposite reaction: switch like <-> dislike
                    existing.setLiked(liked);
                    return likeRepo.save(existing).map(LikeAdapter::toLikeResponseDTO);
                });
    }
}