package com.quoraBackend.consumers;

import com.quoraBackend.adapter.QuestionAdapter;
import com.quoraBackend.events.QuestionEvent;
import com.quoraBackend.models.QuestionElasticDocument;
import com.quoraBackend.repositories.QuestionDocumentRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaQuestionConsumer {

    private final QuestionDocumentRepo questionDocumentRepo;

    @KafkaListener(topics = "question-events", groupId = "elastic-sync-group")
    public void consumeQuestionEvent(QuestionEvent event) {
        log.info("Consuming QuestionEvent [{}] for ID: {}", event.getEventType(), event.getQuestionId());

        switch (event.getEventType()) {
            case CREATED, UPDATED -> {
                QuestionElasticDocument doc = QuestionAdapter.toQuestionElasticDocument(event);
                questionDocumentRepo.save(doc);
                log.info("Successfully indexed question in Elasticsearch: {}", event.getQuestionId());
            }
            case DELETED -> {
                questionDocumentRepo.deleteById(event.getQuestionId());
                log.info("Successfully removed question from Elasticsearch: {}", event.getQuestionId());
            }
        }
    }
}