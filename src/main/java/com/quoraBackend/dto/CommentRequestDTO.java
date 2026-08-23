package com.quoraBackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequestDTO {

    @NotBlank(message = "Content is required")
    @Size(min = 1, max = 500, message = "Comment must be between 1 and 500 characters")
    private String content;

    @NotBlank(message = "UserId is required")
    private String userId;

    @NotBlank(message = "TargetId is required")
    private String targetId;

    @NotBlank(message = "TargetType is required")
    private String targetType;

    private String parentId;
}