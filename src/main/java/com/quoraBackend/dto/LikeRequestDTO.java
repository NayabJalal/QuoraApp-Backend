package com.quoraBackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeRequestDTO {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Target ID is required")
    private String targetId;

    @NotBlank(message = "Target Type is required")
    private String targetType;

    @NotNull(message = "Is Like is required")
    private Boolean liked;
}
