package com.quoraBackend.services;

import com.quoraBackend.adapter.CommentAdapter;
import com.quoraBackend.dto.CommentRequestDTO;
import com.quoraBackend.dto.CommentResponseDTO;
import com.quoraBackend.exceptions.ResourceNotFoundException;
import com.quoraBackend.models.Comment;
import com.quoraBackend.repositories.CommentRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService implements ICommentService {

    private final CommentRepo commentRepo;

    @Override
    public Mono<CommentResponseDTO> createComment(CommentRequestDTO dto) {
        Comment comment = CommentAdapter.toComment(dto);
        return commentRepo.save(comment)
                .map(CommentAdapter::toCommentResponseDTO)
                .doOnSuccess(saved -> log.info("Comment created with id: {}", saved.getId()))
                .doOnError(err -> log.error("Error creating comment", err));
    }

    @Override
    public Flux<CommentResponseDTO> getCommentsByTarget(String targetId, String targetType, int page, int size) {
        return commentRepo.findByTargetIdAndTargetTypeOrderByCreatedAtDesc(targetId, targetType, PageRequest.of(page, size))
                .map(CommentAdapter::toCommentResponseDTO)
                .doOnError(err -> log.error("Error fetching comments for targetId: {}", targetId, err));
    }

    @Override
    public Flux<CommentResponseDTO> getReplies(String parentId, int page, int size) {
        return commentRepo.findByParentIdOrderByCreatedAtAsc(parentId, PageRequest.of(page, size))
                .map(CommentAdapter::toCommentResponseDTO)
                .doOnError(err -> log.error("Error fetching replies for parentId: {}", parentId, err));
    }

    @Override
    public Mono<CommentResponseDTO> updateComment(String commentId, String content) {
        return commentRepo.findById(commentId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Comment not found with id: " + commentId)))
                .flatMap(existing -> {
                    existing.setContent(content);
                    existing.setUpdatedAt(LocalDateTime.now());
                    return commentRepo.save(existing);
                })
                .map(CommentAdapter::toCommentResponseDTO)
                .doOnSuccess(updated -> log.info("Comment updated with id: {}", commentId))
                .doOnError(err -> log.error("Error updating comment with id: {}", commentId, err));
    }

    @Override
    public Mono<Void> deleteComment(String commentId) {
        return commentRepo.findById(commentId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Comment not found with id: " + commentId)))
                .flatMap(commentRepo::delete)
                .doOnSuccess(v -> log.info("Comment deleted with id: {}", commentId))
                .doOnError(err -> log.error("Error deleting comment with id: {}", commentId, err));
    }
}