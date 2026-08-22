package com.quoraBackend.controllers;

import com.quoraBackend.dto.AnswerRequestDTO;
import com.quoraBackend.dto.AnswerResponseDTO;
import com.quoraBackend.services.IAnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/questions/{questionId}/answers")
@RequiredArgsConstructor
public class AnswersController {

    private final IAnswerService answerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AnswerResponseDTO> createAnswer(
            @PathVariable String questionId,
            @Valid @RequestBody AnswerRequestDTO answerRequestDTO){
        return answerService.createAnswer(questionId, answerRequestDTO)
                .doOnSuccess(response ->
                        log.info("Answer created successfully: {}", response))
                .doOnError(error ->
                        log.error("Error creating answer", error));
    }

    @GetMapping("/{answerId}")
    public Mono<AnswerResponseDTO> getAnswerById(
            @PathVariable String questionId,
            @PathVariable String answerId) {
        return answerService.getAnswerById(questionId, answerId)
                .doOnSuccess(response ->
                        log.info("Answer retrieved successfully: {}", response))
                .doOnError(error ->
                        log.error("Error retrieving answer", error));
    }

    @GetMapping
    public Flux<AnswerResponseDTO> getAllAnswersByQuestion(@PathVariable String questionId) {
        return answerService.getAnswersByQuestion(questionId)
                .doOnComplete(() ->
                        log.info("All answers retrieved successfully for questionId: {}", questionId))
                .doOnError(error ->
                        log.error("Error retrieving answers for questionId: {}", questionId, error));
    }

    @PutMapping("/{answerId}")
    public Mono<AnswerResponseDTO> updateAnswer(
            @PathVariable String questionId,
            @PathVariable String answerId,
            @Valid @RequestBody AnswerRequestDTO answerRequestDTO) {
        return answerService.updateAnswer(questionId, answerId, answerRequestDTO)
                .doOnSuccess(response ->
                        log.info("Answer updated successfully: {}", response))
                .doOnError(error ->
                        log.error("Error updating answer", error));
    }

    @DeleteMapping("/{answerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteAnswer(
            @PathVariable String questionId,
            @PathVariable String answerId) {
        return answerService.deleteAnswer(questionId, answerId)
                .doOnSuccess(v ->
                        log.info("Answer deleted successfully with id: {}", answerId))
                .doOnError(error ->
                        log.error("Failed to delete answer with id: {}", answerId, error));
    }

}
