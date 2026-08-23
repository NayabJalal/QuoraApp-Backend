package com.quoraBackend.services;

import com.quoraBackend.dto.CommentRequestDTO;
import com.quoraBackend.dto.CommentResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ICommentService {
    Mono<CommentResponseDTO> createComment(CommentRequestDTO dto);
    Flux<CommentResponseDTO> getCommentsByTarget(String targetId, String targetType, int page, int size);
    Flux<CommentResponseDTO> getReplies(String parentId, int page, int size);
    Mono<CommentResponseDTO> updateComment(String commentId, String content);
    Mono<Void> deleteComment(String commentId);
}