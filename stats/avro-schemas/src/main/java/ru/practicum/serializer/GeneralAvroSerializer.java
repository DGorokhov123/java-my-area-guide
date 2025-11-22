package ru.practicum.serializer;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class GeneralAvroSerializer implements Serializer<SpecificRecordBase> {

    private final EncoderFactory encoderFactory = EncoderFactory.get();

    @Override
    public byte[] serialize(String topic, SpecificRecordBase data) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (data != null) {
                DatumWriter<SpecificRecordBase> writer = new SpecificDatumWriter<>(data.getSchema());
                BinaryEncoder encoder = encoderFactory.binaryEncoder(out, null);
                writer.write(data, encoder);
                encoder.flush();
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new SerializationException("Serialization Error for topic " + topic, e);
        }
    }

}
