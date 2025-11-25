package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dal.EventSimilarity;
import ru.practicum.dal.EventSimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.mapper.EventSimilarityMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSimilarityService {

    private final EventSimilarityRepository eventSimilarityRepository;

    @Transactional
    public void handleEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        log.debug("IN: {}", eventSimilarityAvro);
        String logAction;
        Long first = Math.min(eventSimilarityAvro.getEventA(), eventSimilarityAvro.getEventB());
        Long second = Math.max(eventSimilarityAvro.getEventA(), eventSimilarityAvro.getEventB());

        EventSimilarity eventSimilarity = eventSimilarityRepository.findByEventAAndEventB(first, second);

        if (eventSimilarity == null) {
            eventSimilarity = EventSimilarityMapper.fromAvroToNewEntity(eventSimilarityAvro);
            logAction = "Created";
        } else {
            eventSimilarity.setScore(eventSimilarityAvro.getScore());
            eventSimilarity.setTimestamp(eventSimilarityAvro.getTimestamp());
            logAction = "Updated";
        }
        eventSimilarityRepository.save(eventSimilarity);
        log.debug("{} similarity for {} and {} : Set score to {}",
                logAction, eventSimilarity.getEventA(), eventSimilarity.getEventB(), eventSimilarity.getScore());
    }


}
