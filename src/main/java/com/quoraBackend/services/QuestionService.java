package com.quoraBackend.services;

import com.quoraBackend.adapter.QuestionAdapter;
import com.quoraBackend.dto.QuestionRequestDTO;
import com.quoraBackend.dto.QuestionResponseDTO;
import com.quoraBackend.events.ViewCountEvent;
import com.quoraBackend.models.QuestionElasticDocument;
import com.quoraBackend.models.Questions;
import com.quoraBackend.producers.KafkaEventProducer;
import com.quoraBackend.repositories.QuestionDocumentRepo;
import com.quoraBackend.repositories.QuestionRepo;
import com.quoraBackend.util.CursorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor //No need of constructor injection of QuestionRepo using this annotation
public class QuestionService implements IQuestionService{

    private final QuestionRepo questionRepo;
    private final KafkaEventProducer kafkaEventProducer;
    private final IQuestionIndexService questionIndexService;
    private final QuestionDocumentRepo questionDocumentRepo;

    @Override
    public Mono<QuestionResponseDTO> createQuestion(QuestionRequestDTO questionRequestDTO) {
        List<String> rawTags = questionRequestDTO.getTags();
        List<String> tag = normalizeTags(rawTags);
        Questions questions = Questions.builder()
                .title(questionRequestDTO.getTitle())
                .content(questionRequestDTO.getContent())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .tags(tag)
                .build();

               // save in mongoDb and return a mono of question
        return questionRepo.save(questions)
                .map(savedQuestion -> {
                    questionIndexService.createQuestionIndex(savedQuestion); //dumping the question into elastic Search
                    return QuestionAdapter.toQuestionResponseDTO(savedQuestion);
                })
                .doOnSuccess(response ->
                        log.info("Question created successfully: {}", response))
                .doOnError(error ->
                        log.error("Error creating question", error));
    }

    private List<String> normalizeTags(List<String> tags){
        if (tags == null || tags.isEmpty()) return Collections.emptyList();
        return tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .map(String::toLowerCase)
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
    }

    @Override
    public Mono<QuestionResponseDTO> getQuestionById(String id) {
        return questionRepo.findById(id)
                .map(QuestionAdapter::toQuestionResponseDTO)
                .doOnError(error ->
                        log.error("Error finding question with id: {}", id, error))
                .doOnSuccess(response -> {
                    log.info("Question fetched successfully: {}", response);
                    ViewCountEvent viewCountEvent = new ViewCountEvent(id,"question", LocalDateTime.now());
                    kafkaEventProducer.publishViewCountEvent(viewCountEvent);
                });

    }

    @Override
    public Flux<QuestionResponseDTO> findAll(String cursor , int size) {
        Pageable pageable = PageRequest.of(0,size);
        if (!CursorUtils.isValidCursor(cursor)){
            return questionRepo.findTop10ByOrderByCreatedAtAsc()
                    .take(size)
                    .map(QuestionAdapter::toQuestionResponseDTO)
                    .doOnError(error ->
                            log.error("Error fetching questions", error))
                    .doOnComplete(() ->
                            log.info("Questions fetched successfully"));
        } else {
            LocalDateTime cursorTimeStamp = CursorUtils.parseCursor(cursor);
            return questionRepo.findByCreatedAtGreaterThanOrderByCreatedAtAsc(cursorTimeStamp , pageable)
                    .map(QuestionAdapter::toQuestionResponseDTO)
                    .doOnError(error ->
                            log.error("Error fetching questions", error))
                    .doOnComplete(() ->
                            log.info("Questions fetched successfully"));
        }
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return questionRepo.deleteById(id)
                .doOnSuccess(v ->
                        log.info("Question deleted successfully: {}", id))
                .doOnError(error ->
                        log.error("Failed to delete question with id: {}", id, error));
    }

    @Override
    public Flux<QuestionResponseDTO> searchQuestions(String searchTerm, int offset, int page) {
        return questionRepo.findByTitleOrContentContainingIgnoreCase(searchTerm, PageRequest.of(offset,page))
                .map(QuestionAdapter::toQuestionResponseDTO)
                .doOnError(error ->
                        log.error("Error finding questions for search term: {}", searchTerm, error))
                .doOnComplete(() ->
                        log.info("Questions searched successfully"));
    }
    @Override
    public Flux<String> getAllTags() {
        return questionRepo.findAllTagsOnly()
                .flatMap(q -> {
                    List<String> tags = q.getTags();
                    return tags == null ? Flux.empty() : Flux.fromIterable(tags);
                })
                .filter(Objects::nonNull)
                .distinct()
                .sort();
    }
    @Override
    public Flux<QuestionResponseDTO> searchByTag(List<String> tag, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<String> normalizedTags = normalizeTags(tag);
        return questionRepo.findByTagsIn(normalizedTags, pageable)
                .map(QuestionAdapter::toQuestionResponseDTO)
                .doOnComplete(() ->
                        log.info("Question search by tag completed successfully"))
                .doOnError(error ->
                        log.error("Error searching questions by tag", error));
    }
    @Override
    public Mono<QuestionResponseDTO> deleteTag(String id, String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return Mono.error(new RuntimeException("Tag cannot be empty"));
        }
        String normalizedTag = tag.trim().toLowerCase();
        return questionRepo.removeTagById(id, normalizedTag)
                .flatMap(updatedCount -> {
                    if (updatedCount == 0) {
                        return Mono.error(
                                new RuntimeException("Question not found or tag not present")
                        );
                    }
                    return questionRepo.findById(id);
                })
                .map(QuestionAdapter::toQuestionResponseDTO);
    }
    public List<QuestionElasticDocument> searchQuestionsByElasticsearch(String query){
        return questionDocumentRepo.findByTitleContainingOrContentContaining(query,query);
    }
}
