package com.quoraBackend.services;

import com.quoraBackend.adapter.QuestionAdapter;
import com.quoraBackend.dto.QuestionRequestDTO;
import com.quoraBackend.dto.QuestionResponseDTO;
import com.quoraBackend.models.Questions;
import com.quoraBackend.repositories.QuestionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor //No need of constructor injection of QuestionRepo using this annotation
public class QuestionService implements IQuestionService{

    private final QuestionRepo questionRepo;

    @Override
    public Mono<QuestionResponseDTO> createQuestion(QuestionRequestDTO questionRequestDTO) {
        Questions questions = Questions.builder()
                .title(questionRequestDTO.getTitle())
                .content(questionRequestDTO.getContent())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

               // save in mongoDb and return a mono of question
        return questionRepo.save(questions)
                .map(QuestionAdapter::toQuestionResponseDTO)
                .doOnSuccess(response -> System.out.println("Question created successfully : " + response))
                .doOnError(error -> System.out.println("Error creating question: "+error));

    }

    @Override
    public Mono<QuestionResponseDTO> getById(String id) {
        return questionRepo.findById(id)
                .map(QuestionAdapter::toQuestionResponseDTO)
                .doOnSuccess(response -> System.out.println("Question found: " + response))
                .doOnError(error -> System.out.println("Error finding question: " + error));
    }

    @Override
    public Flux<QuestionResponseDTO> findAll() {
        return questionRepo.findAll()
                .map(QuestionAdapter::toQuestionResponseDTO)
                .doOnError(error -> System.out.println("Internal Error"));
    }
}
