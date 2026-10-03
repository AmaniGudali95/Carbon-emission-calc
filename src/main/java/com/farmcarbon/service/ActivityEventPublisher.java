package com.farmcarbon.service;


import com.farmcarbon.event.ActivityLoggedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ActivityEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(ActivityEventPublisher.class);

    private final KafkaTemplate<String, ActivityLoggedEvent> kafkaTemplate;
    private final String topic;

    public ActivityEventPublisher(
            KafkaTemplate<String, ActivityLoggedEvent> kafkaTemplate,
            @Value("${app.kafka.topic.activity-logged}") String topic
    ) {
        this.kafkaTemplate=kafkaTemplate;
        this.topic=topic;
    }

    public void publishActivityLogged(Long activityId) {
        log.info("Publishing ActivityLoggedEvent for activity id={}", activityId);
        kafkaTemplate.send(topic, activityId.toString(), new ActivityLoggedEvent(activityId));
    }
}
