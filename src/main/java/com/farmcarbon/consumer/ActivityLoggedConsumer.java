package com.farmcarbon.consumer;

import com.farmcarbon.entity.Activity;
import com.farmcarbon.event.ActivityLoggedEvent;
import com.farmcarbon.repository.ActivityRepository;
import com.farmcarbon.service.EmissionCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ActivityLoggedConsumer {
    private static final Logger log = LoggerFactory.getLogger(ActivityLoggedConsumer.class);

    private final ActivityRepository activityRepository;
    private final EmissionCalculationService emissionCalculationService;

    public ActivityLoggedConsumer(
            ActivityRepository activityRepository,
            EmissionCalculationService emissionCalculationService
    ) {
        this.activityRepository=activityRepository;
        this.emissionCalculationService=emissionCalculationService;
    }

    @KafkaListener(topics = "${app.kafka.topic.activity-logged}")
    public void handle(ActivityLoggedEvent event) {
        log.info("Consumed ActivityLoggedEvent for activity id={}", event.activityId());
        Activity activity = activityRepository.findById(event.activityId())
                .orElseThrow(() -> new IllegalStateException("Activity " + event.activityId() + " not found"));
        emissionCalculationService.calculateAndSave(activity);
    }

}
