package com.quoraBackend.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponseDTO {

    private String id;

    private String title;

    private String content;

    private LocalDateTime createdAt;

    private List<String> tags;
}
