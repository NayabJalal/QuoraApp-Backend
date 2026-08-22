package com.quoraBackend.services;

import com.quoraBackend.dto.LikeRequestDTO;
import com.quoraBackend.dto.LikeResponseDTO;
import reactor.core.publisher.Mono;

public interface ILikeService {
    Mono<LikeResponseDTO> createLike(LikeRequestDTO likeRequestDTO);

    Mono<LikeResponseDTO> countLikesByTargetIdAndTargetType(String targetId , String targetType);

    Mono<LikeResponseDTO> countDisLikesByTargetIdAndTargetType(String targetId , String targetType);

    Mono<LikeResponseDTO> toggleLike(String userId, String targetId , String targetType , Boolean liked);
}
