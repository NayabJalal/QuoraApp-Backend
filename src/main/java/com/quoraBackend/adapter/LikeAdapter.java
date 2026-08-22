package com.quoraBackend.adapter;

import com.quoraBackend.dto.LikeRequestDTO;
import com.quoraBackend.dto.LikeResponseDTO;
import com.quoraBackend.models.Like;

import java.time.LocalDateTime;

public class LikeAdapter {
    public static LikeResponseDTO toLikeResponseDTO(Like like) {
        return LikeResponseDTO.builder()
                .id(like.getId())
                .userId(like.getUserId())
                .targetId(like.getTargetId())
                .targetType(like.getTargetType())
                .liked(like.getLiked())
                .createdAt(like.getCreatedAt())
                .build();
    }
    public static Like toLike(LikeRequestDTO likeRequestDTO) {
        return Like.builder()
                .userId(likeRequestDTO.getUserId())
                .targetId(likeRequestDTO.getTargetId())
                .targetType(likeRequestDTO.getTargetType())
                .liked(likeRequestDTO.getLiked())
                .build();
    }

}