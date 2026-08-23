package com.quoraBackend.services;

import com.quoraBackend.dto.QuestionRequestDTO;
import com.quoraBackend.dto.QuestionResponseDTO;
import com.quoraBackend.models.QuestionElasticDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IQuestionService {
    public Mono<QuestionResponseDTO> createQuestion(QuestionRequestDTO questionRequestDTO);

    public Mono<QuestionResponseDTO> getQuestionById(String id); //Update the corresponding view count

    public Flux<QuestionResponseDTO> findAll(String cursor , int size);

    public Mono<Void> deleteById(String id);

    Flux<QuestionResponseDTO> searchQuestions(String searchTerm, int page, int size);

    public Flux<QuestionResponseDTO> searchByTag(List<String> tag, int page, int size);

    public Flux<String> getAllTags();

    Mono<QuestionResponseDTO> deleteTag(String id, String tag);

    List<QuestionElasticDocument> searchQuestionsByElasticsearch(String query);

    Mono<QuestionResponseDTO> updateQuestion(String id, QuestionRequestDTO questionRequestDTO);

    Mono<QuestionResponseDTO> addTags(String id, List<String> tags);
}
