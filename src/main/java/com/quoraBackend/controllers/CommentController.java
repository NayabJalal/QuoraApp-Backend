package com.quoraBackend.controllers;

import com.quoraBackend.dto.CommentRequestDTO;
import com.quoraBackend.dto.CommentResponseDTO;
import com.quoraBackend.services.ICommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final ICommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CommentResponseDTO> createComment(@Valid @RequestBody CommentRequestDTO dto) {
        return commentService.createComment(dto);
    }

    @GetMapping
    public Flux<CommentResponseDTO> getCommentsByTarget(
            @RequestParam String targetId,
            @RequestParam String targetType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return commentService.getCommentsByTarget(targetId, targetType, page, size);
    }

    @GetMapping("/{commentId}/replies")
    public Flux<CommentResponseDTO> getReplies(
            @PathVariable String commentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return commentService.getReplies(commentId, page, size);
    }

    @PutMapping("/{commentId}")
    public Mono<CommentResponseDTO> updateComment(
            @PathVariable String commentId,
            @RequestParam String content
    ) {
        return commentService.updateComment(commentId, content);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteComment(@PathVariable String commentId) {
        return commentService.deleteComment(commentId);
    }
}