package com.quoraBackend.services;

import com.quoraBackend.adapter.CommentAdapter;
import com.quoraBackend.dto.CommentRequestDTO;
import com.quoraBackend.dto.CommentResponseDTO;
import com.quoraBackend.exceptions.ResourceNotFoundException;
import com.quoraBackend.models.Comment;
import com.quoraBackend.repositories.CommentRepo;
import com.quoraBackend.repositories.LikeRepo;
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
    private final LikeRepo likeRepo;

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
        return commentRepo.findByTargetIdAndTargetTypeAndParentIdIsNullOrderByCreatedAtDesc(
                targetId, targetType, PageRequest.of(page, size)
        ).flatMap(comment ->
                Mono.zip(
                        commentRepo.countByParentId(comment.getId()),
                        commentRepo.findByParentIdOrderByCreatedAtAsc(comment.getId(), PageRequest.of(0, 2))
                                .flatMap(previewReply ->
                                        // Enrich each preview reply with its own replyCount
                                        commentRepo.countByParentId(previewReply.getId())
                                                .map(count -> {
                                                    CommentResponseDTO dto = CommentAdapter.toCommentResponseDTO(previewReply);
                                                    dto.setReplyCount(count);
                                                    return dto;
                                                })
                                )
                                .collectList()
                ).map(tuple -> {
                    CommentResponseDTO dto = CommentAdapter.toCommentResponseDTO(comment);
                    dto.setReplyCount(tuple.getT1());
                    dto.setPreviewReplies(tuple.getT2());
                    return dto;
                })
        ).doOnError(err -> log.error("Error fetching comments for targetId: {}", targetId, err));
    }

    @Override
    public Flux<CommentResponseDTO> getReplies(String parentId, int page, int size) {
        return commentRepo.findByParentIdOrderByCreatedAtAsc(parentId, PageRequest.of(page, size))
                .flatMap(reply ->
                        // Enrich each reply with its direct child count
                        commentRepo.countByParentId(reply.getId())
                                .map(count -> {
                                    CommentResponseDTO dto = CommentAdapter.toCommentResponseDTO(reply);
                                    dto.setReplyCount(count);
                                    return dto;
                                })
                )
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
                .doOnSuccess(updated -> log.info("Comment updated with id: {}", commentId));
    }

    @Override
    public Mono<Void> deleteComment(String commentId) {
        return commentRepo.findById(commentId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Comment not found with id: " + commentId)))
                .flatMap(comment ->
                        commentRepo.deleteByParentId(commentId)
                                .then(likeRepo.deleteByTargetId(commentId))
                                .then(commentRepo.delete(comment))
                );
    }
}