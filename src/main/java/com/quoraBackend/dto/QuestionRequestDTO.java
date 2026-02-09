package com.quoraBackend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequestDTO {

    @NotBlank(message = "Title is required")
    @Size(min = 10 , max = 100, message = "Title must be between 10 and 100 characters!!") //@Size annotation is used to validate the length of a String or the number of elements in a Collection, Map, or Array
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min = 10 , max = 1000, message = "Content must be between 10 and 100 characters!!") //@Size annotation is used to validate the length of a String or the number of elements in a Collection, Map, or Array
    private String content;

    @NotBlank(message = "Tags are required")
    @Size(max = 5, message = "You can only have up to 5 tags total")
    private List<@Size(min = 3, max = 30, message = "Each tag must be 3-30 characters!") String> tags;
}
