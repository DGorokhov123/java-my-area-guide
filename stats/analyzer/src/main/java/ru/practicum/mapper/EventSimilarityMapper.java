package ru.practicum.mapper;

import ru.practicum.dal.EventSimilarity;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

public class EventSimilarityMapper {

    public static EventSimilarity fromAvroToNewEntity(EventSimilarityAvro avro) {
        Long first = Math.min(avro.getEventA(), avro.getEventB());
        Long second = Math.max(avro.getEventA(), avro.getEventB());
        return EventSimilarity.builder()
                .eventA(first)
                .eventB(second)
                .score(avro.getScore())
                .timestamp(avro.getTimestamp())
                .build();
    }

}
