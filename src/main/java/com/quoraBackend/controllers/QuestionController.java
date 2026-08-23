package com.quoraBackend.controllers;

import com.quoraBackend.dto.QuestionRequestDTO;
import com.quoraBackend.dto.QuestionResponseDTO;
import com.quoraBackend.models.QuestionElasticDocument;
import com.quoraBackend.services.IQuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final IQuestionService questionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<QuestionResponseDTO> createQuestion(@Valid @RequestBody QuestionRequestDTO questionRequestDTO){

        return questionService.createQuestion(questionRequestDTO)
                .doOnSuccess(response ->
                        log.info("Question created successfully: {}", response))
                .doOnError(error ->
                        log.error("Error creating question", error));
    }

    @GetMapping("/{id}")
    public Mono<QuestionResponseDTO> getById(@PathVariable String id){
        return questionService.getQuestionById(id)
                .doOnSuccess(response ->
                        log.info("Question fetched successfully: {}", response))
                .doOnError(error ->
                        log.error("Error fetching question", error));
    }

    @GetMapping
    public Flux<QuestionResponseDTO> getAllQuestions(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size
    ){
        return questionService.findAll(cursor, size)
                .doOnError(error ->
                        log.error("Error fetching questions", error))
                .doOnComplete(() ->
                        log.info("Questions fetched successfully"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteById(@PathVariable String id){
        return questionService.deleteById(id)
                .doOnSuccess(v ->
                        log.info("Question deleted successfully with id: {}", id))
                .doOnError(error ->
                        log.error("Failed to delete question with id: {}", id, error));
    }

    @GetMapping("/search")
    public Flux<QuestionResponseDTO> searchQuestions(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return questionService.searchQuestions(query, page, size);
    }

    @GetMapping("/tags/all")
    public Mono<List<String>> getAllTags() {
        return questionService.getAllTags()
                .collectList();
    }

    @GetMapping("/tags")
    public Flux<QuestionResponseDTO> getByTag(
            @RequestParam List<String> tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return questionService.searchByTag(tag,page,size);
    }

    @DeleteMapping("/{id}/tags")
    public Mono<QuestionResponseDTO> deleteTag(
            @PathVariable String id,
            @RequestParam String tag
    ) {
        return questionService.deleteTag(id, tag);
    }

    @GetMapping("/elasticsearch")
    public List<QuestionElasticDocument> searchQuestionByElasticsearch(@RequestParam String query){
        return questionService.searchQuestionsByElasticsearch(query);
    }

    @PutMapping("/{id}")
    public Mono<QuestionResponseDTO> updateQuestion(
            @PathVariable String id,
            @Valid @RequestBody QuestionRequestDTO questionRequestDTO
    ) {
        return questionService.updateQuestion(id, questionRequestDTO)
                .doOnSuccess(response -> log.info("Question updated successfully: {}", response))
                .doOnError(error -> log.error("Error updating question with id: {}", id, error));
    }

    @PostMapping("/{id}/tags")
    public Mono<QuestionResponseDTO> addTags(
            @PathVariable String id,
            @RequestParam List<String> tags
    ) {
        return questionService.addTags(id, tags)
                .doOnSuccess(response -> log.info("Tags added successfully to question: {}", id))
                .doOnError(error -> log.error("Error adding tags to question with id: {}", id, error));
    }
}
