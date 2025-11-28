package ru.practicum.deserializer;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

public class EventsSimilarityAvroDeserializer extends AbstractAvroDeserializer<EventSimilarityAvro> {

    public EventsSimilarityAvroDeserializer() {
        super(EventSimilarityAvro.getClassSchema());
    }

}
