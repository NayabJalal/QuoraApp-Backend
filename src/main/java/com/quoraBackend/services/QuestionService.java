package com.quoraBackend.services;

import com.quoraBackend.adapter.QuestionAdapter;
import com.quoraBackend.dto.QuestionRequestDTO;
import com.quoraBackend.dto.QuestionResponseDTO;
import com.quoraBackend.models.Questions;
import com.quoraBackend.repositories.QuestionRepo;
import com.quoraBackend.util.CursorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor //No need of constructor injection of QuestionRepo using this annotation
public class QuestionService implements IQuestionService{

    private final QuestionRepo questionRepo;

    @Override
    public Mono<QuestionResponseDTO> createQuestion(QuestionRequestDTO questionRequestDTO) {
        List<String> rawTags = questionRequestDTO.getTags();
        List<String> tag = normalizeTags(rawTags);
        Questions questions = Questions.builder()
                .title(questionRequestDTO.getTitle())
                .content(questionRequestDTO.getContent())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .tags(tag)
                .build();

               // save in mongoDb and return a mono of question
        return questionRepo.save(questions)
                .map(QuestionAdapter::toQuestionResponseDTO)
                .doOnSuccess(response -> System.out.println("Question created successfully : " + response))
                .doOnError(error -> System.out.println("Error creating question: "+error));

    }
    private List<String> normalizeTags(List<String> tags){
        if (tags == null || tags.isEmpty()) return Collections.emptyList();
        return tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .map(String::toLowerCase)
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
    }

    @Override
    public Mono<QuestionResponseDTO> getById(String id) {
        return questionRepo.findById(id)
                .map(QuestionAdapter::toQuestionResponseDTO)
                .doOnSuccess(response -> System.out.println("Question found: " + response))
                .doOnError(error -> System.out.println("Error finding question: " + error));
    }

    @Override
    public Flux<QuestionResponseDTO> findAll(String cursor , int size) {
        Pageable pageable = PageRequest.of(0,size);
        if (!CursorUtils.isValidCursor(cursor)){
            return questionRepo.findTop10ByOrderByCreatedAtAsc()
                    .take(size)
                    .map(QuestionAdapter::toQuestionResponseDTO)
                    .doOnError(error -> System.out.println("Error Fetching Question"))
                    .doOnComplete(() -> System.out.println("Question Fetched Successfully: "));
        } else {
            LocalDateTime cursorTimeStamp = CursorUtils.parseCursor(cursor);
            return questionRepo.findByCreatedAtGreaterThanOrderByCreatedAtAsc(cursorTimeStamp , pageable)
                    .map(QuestionAdapter::toQuestionResponseDTO)
                    .doOnError(error -> System.out.println("Error Fetching Question"))
                    .doOnComplete(() -> System.out.println("Question Fetched Successfully: "));
        }
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return questionRepo.deleteById(id)
                .doOnSuccess(v -> System.out.println("Successfully deleted the Question : "+id))
                .doOnError(error -> System.out.println("Failed to delete id : "+ id));
    }

    @Override
    public Flux<QuestionResponseDTO> searchQuestions(String searchTerm, int offset, int page) {
        return questionRepo.findByTitleOrContentContainingIgnoreCase(searchTerm, PageRequest.of(offset,page))
                .map(QuestionAdapter::toQuestionResponseDTO)
                .doOnError(error -> System.out.println("Error finding questions: " + error))
                .doOnComplete(() -> System.out.println("Questions Searched Successfully"));
    }
}
