package com.quoraBackend.services;

import com.quoraBackend.adapter.QuestionAdapter;
import com.quoraBackend.dto.QuestionRequestDTO;
import com.quoraBackend.dto.QuestionResponseDTO;
import com.quoraBackend.events.ViewCountEvent;
import com.quoraBackend.exceptions.InvalidRequestException;
import com.quoraBackend.exceptions.ResourceNotFoundException;
import com.quoraBackend.models.Answer;
import com.quoraBackend.models.QuestionElasticDocument;
import com.quoraBackend.models.Questions;
import com.quoraBackend.producers.KafkaEventProducer;
import com.quoraBackend.repositories.*;
import com.quoraBackend.util.CursorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService implements IQuestionService {

    private final QuestionRepo questionRepo;
    private final AnswersRepo answersRepo;
    private final CommentRepo commentRepo;
    private final LikeRepo likeRepo;
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

        return questionRepo.save(questions)
                .map(QuestionAdapter::toQuestionResponseDTO)
                .doOnSuccess(response ->
                        log.info("Question created successfully with id: {}", response.getId()))
                .doOnError(error ->
                        log.error("Error creating question", error));
    }

    private List<String> normalizeTags(List<String> tags) {
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
                    ViewCountEvent viewCountEvent = new ViewCountEvent(id, "question", LocalDateTime.now());
                    kafkaEventProducer.publishViewCountEvent(viewCountEvent);
                });
    }

    @Override
    public Flux<QuestionResponseDTO> findAll(String cursor, int size) {
        Pageable pageable = PageRequest.of(0, size);
        if (!CursorUtils.isValidCursor(cursor)) {
            return questionRepo.findTop10ByOrderByCreatedAtAsc()
                    .take(size)
                    .map(QuestionAdapter::toQuestionResponseDTO)
                    .doOnError(error ->
                            log.error("Error fetching questions", error))
                    .doOnComplete(() ->
                            log.info("Questions fetched successfully"));
        } else {
            LocalDateTime cursorTimeStamp = CursorUtils.parseCursor(cursor);
            return questionRepo.findByCreatedAtGreaterThanOrderByCreatedAtAsc(cursorTimeStamp, pageable)
                    .map(QuestionAdapter::toQuestionResponseDTO)
                    .doOnError(error ->
                            log.error("Error fetching questions", error))
                    .doOnComplete(() ->
                            log.info("Questions fetched successfully"));
        }
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return questionRepo.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Question not found with id: " + id)))
                .flatMap(question ->
                        // Step 1: Fetch all Answer IDs linked to this question
                        answersRepo.findByQuestionId(id)
                                .map(Answer::getId)
                                .collectList()
                                .flatMap(answerIds -> {
                                    // Step 2: Combine Question ID + Answer IDs into one target list
                                    List<String> allTargetIds = new ArrayList<>(answerIds);
                                    allTargetIds.add(id);

                                    // Step 3: Cascading delete comments, likes, and answers concurrently
                                    return Mono.when(
                                            commentRepo.deleteByTargetIdIn(allTargetIds),
                                            likeRepo.deleteByTargetIdIn(allTargetIds),
                                            answersRepo.deleteByQuestionId(id)
                                    );
                                })
                                // Step 4: Finally delete the question document
                                .then(questionRepo.delete(question))
                )
                .doOnSuccess(v -> log.info("Question and all related answers/comments/likes deleted for id: {}", id))
                .doOnError(error -> log.error("Failed to delete question with id: {}", id, error));
    }

    @Override
    public Flux<QuestionResponseDTO> searchQuestions(String searchTerm, int page, int size) {
        return questionRepo.findByTitleOrContentContainingIgnoreCase(searchTerm, PageRequest.of(page, size))
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
            return Mono.error(new InvalidRequestException("Tag cannot be empty"));
        }
        String normalizedTag = tag.trim().toLowerCase();
        return questionRepo.removeTagById(id, normalizedTag)
                .flatMap(updatedCount -> {
                    if (updatedCount == 0) {
                        return Mono.error(
                                new ResourceNotFoundException("Question not found or tag not present")
                        );
                    }
                    return questionRepo.findById(id);
                })
                .map(QuestionAdapter::toQuestionResponseDTO);
    }

    @Override
    public List<QuestionElasticDocument> searchQuestionsByElasticsearch(String query) {
        return questionDocumentRepo.findByTitleContainingOrContentContaining(query, query);
    }

    @Override
    public Mono<QuestionResponseDTO> updateQuestion(String id, QuestionRequestDTO questionRequestDTO) {
        List<String> normalizedTags = normalizeTags(questionRequestDTO.getTags());
        return questionRepo.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Question not found with id: " + id)))
                .flatMap(existingQuestion -> {
                    existingQuestion.setTitle(questionRequestDTO.getTitle());
                    existingQuestion.setContent(questionRequestDTO.getContent());
                    existingQuestion.setTags(normalizedTags);
                    existingQuestion.setUpdatedAt(LocalDateTime.now());
                    return questionRepo.save(existingQuestion);
                })
                .map(QuestionAdapter::toQuestionResponseDTO)
                .doOnSuccess(response -> log.info("Question updated successfully with id: {}", id))
                .doOnError(error -> log.error("Error updating question with id: {}", id, error));
    }

    @Override
    public Mono<QuestionResponseDTO> addTags(String id, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Mono.error(new InvalidRequestException("Tags list cannot be empty"));
        }
        return questionRepo.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Question not found with id: " + id)))
                .flatMap(existingQuestion -> {
                    List<String> currentTags = existingQuestion.getTags() != null
                            ? new ArrayList<>(existingQuestion.getTags())
                            : new ArrayList<>();
                    currentTags.addAll(tags);

                    existingQuestion.setTags(normalizeTags(currentTags));
                    existingQuestion.setUpdatedAt(LocalDateTime.now());
                    return questionRepo.save(existingQuestion);
                })
                .map(QuestionAdapter::toQuestionResponseDTO);
    }
}