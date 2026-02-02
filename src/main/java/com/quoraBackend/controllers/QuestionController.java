package com.quoraBackend.controllers;

import com.quoraBackend.dto.QuestionRequestDTO;
import com.quoraBackend.dto.QuestionResponseDTO;
import com.quoraBackend.services.IQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final IQuestionService questionService;

    @PostMapping
    public Mono<QuestionResponseDTO> createQuestion(@RequestBody QuestionRequestDTO questionRequestDTO){

        return questionService.createQuestion(questionRequestDTO)
                .doOnSuccess(response -> System.out.println("Question created successfully : " + response))
                .doOnError(error -> System.out.println("Error creating question: "+error));
    }

    @GetMapping("/{id}")
    public Mono<QuestionResponseDTO> getById(@PathVariable String id){
        return questionService.getById(id)
                .doOnSuccess(response -> System.out.println("Question fetched successfully: " + response))
                .doOnError(error -> System.out.println("Error fetching question: " + error));
    }
    @GetMapping
    public Flux<QuestionResponseDTO> getAll(){
        return questionService.findAll()
                .doOnError(error -> System.out.println("Internal Error: "+ error));
    }
}
