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
public class LikeResponseDTO {
    private String id;

    private String userId;

    private String targetId;

    private String targetType;

    private Boolean liked;

    private LocalDateTime createdAt;

    private Long count;
}
