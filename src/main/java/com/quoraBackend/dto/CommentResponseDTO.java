package com.quoraBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDTO {

    private String id;
    private String content;
    private String userId;
    private String targetId;
    private String targetType;
    private String parentId;
    private LocalDateTime createdAt;
}