package com.quoraBackend.services;

import com.quoraBackend.adapter.AnswerAdapter;
import com.quoraBackend.dto.AnswerRequestDTO;
import com.quoraBackend.dto.AnswerResponseDTO;
import com.quoraBackend.events.ViewCountEvent;
import com.quoraBackend.exceptions.ResourceNotFoundException;
import com.quoraBackend.models.Answer;
import com.quoraBackend.producers.KafkaEventProducer;
import com.quoraBackend.repositories.AnswersRepo;
import com.quoraBackend.repositories.CommentRepo;
import com.quoraBackend.repositories.LikeRepo;
import com.quoraBackend.repositories.QuestionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnswerService implements IAnswerService{

    private final AnswersRepo answersRepo;
    private final QuestionRepo questionRepo;
    private final LikeRepo likeRepo;
    private final CommentRepo commentRepo;
    private final KafkaEventProducer kafkaEventProducer;

    @Override
    public Mono<AnswerResponseDTO> createAnswer(String questionId, AnswerRequestDTO answerRequestDTO) {
        return questionRepo.findById(questionId)
                .flatMap(questions -> {
                    Answer answer = AnswerAdapter.toEntity(answerRequestDTO, questionId);
                    return answersRepo.save(answer);
                })
                .map(AnswerAdapter::toAnswerResponseDTO)
                .doOnSuccess(response ->
                        log.info("Answer created successfully: {}", response)
                )
                .doOnError(error ->
                        log.error("Error creating answer", error)
                );
    }

    @Override
    public Mono<AnswerResponseDTO> getAnswerById(String questionId, String answerId) {
        return answersRepo.findByIdAndQuestionId(answerId, questionId)
                .map(AnswerAdapter::toAnswerResponseDTO)
                .doOnSuccess(response -> {
                    if (response != null){
                        log.info("\"Answer retrieved successfully: {}",response);
                        ViewCountEvent viewCountEvent = new ViewCountEvent(answerId, "answer", LocalDateTime.now()); // Increment view count by 1
                        kafkaEventProducer.publishViewCountEvent(viewCountEvent); // Publish view count event to Kafka
                    }
                })
                .doOnError(error ->
                        log.error("Error retrieving answer", error));
    }

    @Override
    public Flux<AnswerResponseDTO> getAnswersByQuestion(String questionId) {
        return answersRepo.findByQuestionId(questionId)
                .map(AnswerAdapter::toAnswerResponseDTO)
                .doOnComplete(() ->
                        log.info("All answers retrieved successfully for questionId: {}", questionId)
                )
                .doOnError(error ->
                        log.error("Error retrieving answers for questionId: {}", questionId, error)
                );
    }

    @Override
    public Mono<AnswerResponseDTO> updateAnswer(String questionId, String answerId, AnswerRequestDTO answerRequestDTO) {
        return answersRepo.findByIdAndQuestionId(answerId, questionId)
                .flatMap(existingAnswer -> {
                    existingAnswer.setContent(answerRequestDTO.getContent());
                    existingAnswer.setUpdatedAt(LocalDateTime.now());
                    return answersRepo.save(existingAnswer);
                })
                .map(AnswerAdapter::toAnswerResponseDTO)
                .doOnSuccess(response ->
                        log.info("Answer updated successfully: {}", response)
                )
                .doOnError(error ->
                        log.error("Error updating answer with id: {}", answerId, error)
                );
    }

    @Override
    public Mono<Void> deleteAnswer(String questionId, String answerId) {
        return answersRepo.findByIdAndQuestionId(answerId, questionId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Answer not found with id: " + answerId + " for question: " + questionId)))
                .flatMap(answer ->
                        // 1. Delete all comments under this answer
                        commentRepo.deleteByTargetId(answerId)
                                // 2. Delete all likes for this answer
                                .then(likeRepo.deleteByTargetId(answerId))
                                // 3. Delete the answer itself
                                .then(answersRepo.delete(answer))
                );
    }
}
