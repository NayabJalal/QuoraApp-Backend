package com.quoraBackend.producers;

import com.quoraBackend.config.KafkaConfig;
import com.quoraBackend.events.ViewCountEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventProducer {
    private final KafkaTemplate<String,Object> kafkaTemplate;

    public void publishViewCountEvent(ViewCountEvent viewCountEvent){
        kafkaTemplate.send(KafkaConfig.TOPIC_NAME,viewCountEvent.getTargetId() , viewCountEvent) //key, value - viewCount
                .whenComplete((result , err) -> {
                    if (err!=null){
                        log.info("\"Error Publishing view count event : {}",err.getMessage());
                    }
                });
    }
}
