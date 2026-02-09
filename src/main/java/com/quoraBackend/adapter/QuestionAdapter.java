package com.quoraBackend.adapter;

import com.quoraBackend.dto.QuestionResponseDTO;
import com.quoraBackend.models.Questions;

public class QuestionAdapter {

    //to take a Question and convert it into a question Response
    public static QuestionResponseDTO toQuestionResponseDTO(Questions questions){
        return QuestionResponseDTO.builder()
                .id(questions.getId())
                .title(questions.getTitle())
                .content(questions.getContent())
                .createdAt(questions.getCreatedAt())
                .tags(questions.getTags())
                .build();
    }
}
