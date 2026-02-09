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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "questions") //It is used to identify a domain object, which is persisted to MongoDB.
                                    //So you can use it to map a Java class into a collection inside MongoDB.
public class Questions {
    @Id
    private String id;

    @NotBlank(message = "Title is required")
    @Size(min = 10 , max = 100, message = "Title must be between 10 and 100 characters!!") //@Size annotation is used to validate the length of a String or the number of elements in a Collection, Map, or Array
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min = 10 , max = 1000, message = "Content must be between 10 and 100 characters!!") //@Size annotation is used to validate the length of a String or the number of elements in a Collection, Map, or Array
    private String content;

    @CreatedDate //automatically captures the timestamp when an entity is first saved, Add @EnableJpaAuditing
    private LocalDateTime createdAt;

    @LastModifiedDate //automatically updates a field with the current timestamp whenever the entity is updated in the database.
    private LocalDateTime updatedAt;

    @NotBlank(message = "Tags are required")
    @Size(max = 5, message = "You can only have up to 5 tags total")
    private List<@Size(min = 3, max = 30, message = "Each tag must be 3-30 characters!") String> tags;
}
