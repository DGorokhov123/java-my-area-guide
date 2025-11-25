package ru.practicum.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.properties.CustomProperties;
import ru.practicum.service.EventSimilarityService;
import ru.practicum.service.UserActionService;

@Service
@RequiredArgsConstructor
public class KafkaController {

    private final KafkaListenerEndpointRegistry kafkaRegistry;

    private final CustomProperties customProperties;
    private final UserActionService userActionService;
    private final EventSimilarityService eventSimilarityService;

    @EventListener(ApplicationReadyEvent.class)
    public void initKafkaProducer() {
        kafkaRegistry.start();
    }

    @KafkaListener(
            topics = "#{customProperties.kafka.userActionTopic}",
            containerFactory = "userActionListenerContainerFactory"
    )
    public void listenUserAction(UserActionAvro userActionAvro) {
        userActionService.handleUserAction(userActionAvro);
    }

    @KafkaListener(
            topics = "#{customProperties.kafka.eventsSimilarityTopic}",
            containerFactory = "eventsSimilarityListenerContainerFactory"
    )
    public void listenEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        eventSimilarityService.handleEventSimilarity(eventSimilarityAvro);
    }

}
