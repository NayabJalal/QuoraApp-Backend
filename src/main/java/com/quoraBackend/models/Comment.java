package com.quoraBackend.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "comments")
public class Comment {

    @Id
    private String id;

    @NotBlank(message = "Content is required")
    @Size(min = 1, max = 500, message = "Comment must be between 1 and 500 characters")
    private String content;

    @NotBlank(message = "UserId is required")
    private String userId;

    @NotBlank(message = "TargetId is required")
    private String targetId;

    @NotBlank(message = "TargetType is required")
    private String targetType; // "Question" or "Answer"

    private String parentId;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}