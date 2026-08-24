package com.quoraBackend.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionEvent {

    public enum EventType { CREATED, UPDATED, DELETED }

    private String questionId;
    private EventType eventType;
    private String title;
    private String content;
    private List<String> tags;
    private LocalDateTime timestamp;
}