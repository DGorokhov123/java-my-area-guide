package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dal.UserAction;
import ru.practicum.dal.UserActionRepository;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.properties.CustomProperties;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionService {

    private final CustomProperties customProperties;

    private final UserActionRepository userActionRepository;

    @Transactional
    public void handleUserAction(UserActionAvro userActionAvro) {
        log.debug("IN: {}", userActionAvro);

        BigDecimal weight = customProperties.getAnalyzer().getWeights().ofUserAction(userActionAvro);

        UserAction userAction = UserAction.builder()
                .userId(userActionAvro.getUserId())
                .eventId(userActionAvro.getEventId())
                .weight(weight)
                .timestamp(userActionAvro.getTimestamp())
                .build();

        userActionRepository.save(userAction);
        log.debug("Created user action: {}", userAction);
    }

}
