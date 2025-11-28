package ru.practicum.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaProducerInitializer {

    private final KafkaTemplate<Void, SpecificRecordBase> kafkaTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void initKafkaProducer() {
        kafkaTemplate.flush();
    }

}
