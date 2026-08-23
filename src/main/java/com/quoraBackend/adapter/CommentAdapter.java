package com.quoraBackend.adapter;

import com.quoraBackend.dto.CommentRequestDTO;
import com.quoraBackend.dto.CommentResponseDTO;
import com.quoraBackend.models.Comment;

import java.time.LocalDateTime;

public class CommentAdapter {

    public static Comment toComment(CommentRequestDTO dto) {
        return Comment.builder()
                .content(dto.getContent())
                .userId(dto.getUserId())
                .targetId(dto.getTargetId())
                .targetType(dto.getTargetType())
                .parentId(dto.getParentId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static CommentResponseDTO toCommentResponseDTO(Comment comment) {
        return CommentResponseDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userId(comment.getUserId())
                .targetId(comment.getTargetId())
                .targetType(comment.getTargetType())
                .parentId(comment.getParentId())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}