package com.quoraBackend.adapter;

import com.quoraBackend.dto.QuestionResponseDTO;
import com.quoraBackend.events.QuestionEvent;
import com.quoraBackend.models.QuestionElasticDocument;
import com.quoraBackend.models.Questions;

import java.time.LocalDateTime;

public class QuestionAdapter {

    //to take a Question and convert it into a question Response
    public static QuestionResponseDTO toQuestionResponseDTO(Questions questions){
        return QuestionResponseDTO.builder()
                .id(questions.getId())
                .title(questions.getTitle())
                .content(questions.getContent())
                .createdAt(questions.getCreatedAt())
                .tags(questions.getTags())
                .build();
    }
    public static QuestionEvent toQuestionEvent(QuestionResponseDTO dto, QuestionEvent.EventType eventType) {
        return QuestionEvent.builder()
                .questionId(dto.getId())
                .eventType(eventType)
                .title(dto.getTitle())
                .content(dto.getContent())
                .tags(dto.getTags())
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static QuestionEvent toDeleteQuestionEvent(String questionId) {
        return QuestionEvent.builder()
                .questionId(questionId)
                .eventType(QuestionEvent.EventType.DELETED)
                .timestamp(LocalDateTime.now())
                .build();
    }
    public static QuestionElasticDocument toQuestionElasticDocument(QuestionEvent event) {
        return QuestionElasticDocument.builder()
                .id(event.getQuestionId())
                .title(event.getTitle())
                .content(event.getContent())
                .tags(event.getTags())
                .build();
    }
}
