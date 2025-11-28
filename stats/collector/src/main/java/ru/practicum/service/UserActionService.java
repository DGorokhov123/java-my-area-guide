package ru.practicum.service;

import com.google.protobuf.TextFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.user.action.UserActionProto;
import ru.practicum.mapper.UserActionMapper;
import ru.practicum.properties.CustomProperties;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionService {

    private final KafkaTemplate<Void, SpecificRecordBase> kafkaTemplate;
    private final CustomProperties customProperties;

    public void handleUserAction(UserActionProto userActionProto) {
        log.debug("Received Proto: {}", TextFormat.printer().emittingSingleLine(true).printToString(userActionProto));
        UserActionAvro userActionAvro = UserActionMapper.fromProtoToAvro(userActionProto);
        kafkaTemplate.send(customProperties.getKafka().getUserActionTopic(), userActionAvro);
        log.debug("Sent Avro: {}", userActionAvro);
    }

}
