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
public class AnswerRequestDTO {

    @NotBlank(message = "Content is required")
    @Size(min = 10 , max = 1000, message = "Content must be between 10 and 1000 characters!!")
    private String content;

    @NotBlank(message = "Author Id is required")
    private String authorId;

}
