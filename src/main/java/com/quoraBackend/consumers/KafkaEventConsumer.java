package com.quoraBackend.consumers;

import com.quoraBackend.config.KafkaConfig;
import com.quoraBackend.events.ViewCountEvent;
import com.quoraBackend.repositories.QuestionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

//@Component - Both will create bean but service will give more advantage via business logic
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventConsumer {
    private final QuestionRepo questionRepo;

    //method to handle view count -
    @KafkaListener(
            topics = KafkaConfig.TOPIC_NAME,
            groupId = "view-count-consumer",
            containerFactory = "kafkaListenerContainerFactory"

    )
    public void handleViewCountEvent(ViewCountEvent viewCountEvent){
        //if()viewCount -> targetType = "question"
        //else if "answer" ...."comment" and many more(n number of events)!!!!!
        // This violet the Open Closed Principle - Open for extension closed for modification
        //Strategy Pattern comes into picture --

        questionRepo.findById(viewCountEvent.getTargetId())
                .flatMap(questions -> {
                    log.info("Incrementing view count for question: {}", questions.getId());                    Integer views = questions.getViews();
                    questions.setViews(views ==null ? 0 : views + 1);
                    return questionRepo.save(questions); //return another mono.
                        })
                .subscribe(updatedQuestion -> {
                    log.info("View Count increment for question: {}", updatedQuestion.getId());
                },error -> {
                    log.info("\"Error increment view count for question : {}", error.getMessage());
                });
    }
}
