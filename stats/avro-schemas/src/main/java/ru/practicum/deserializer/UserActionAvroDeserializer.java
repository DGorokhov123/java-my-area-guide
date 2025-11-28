package ru.practicum.deserializer;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public class UserActionAvroDeserializer extends AbstractAvroDeserializer<UserActionAvro> {

    public UserActionAvroDeserializer() {
        super(UserActionAvro.getClassSchema());
    }

}
