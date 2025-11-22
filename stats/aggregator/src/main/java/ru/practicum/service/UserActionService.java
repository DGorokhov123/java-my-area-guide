package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.properties.CustomProperties;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionService {

    private final KafkaTemplate<Void, SpecificRecordBase> kafkaTemplate;
    private final CustomProperties customProperties;

    // Map<userId, Map<eventId, weight>> - таблица весов с быстрым доступом по userId
    private final Map<Long, Map<Long, BigDecimal>> weightsByUser = new HashMap<>();
    // Map<eventId, Map<userId, weight>> - таблица весов с быстрым доступом по eventId
    private final Map<Long, Map<Long, BigDecimal>> weightsByEvent = new HashMap<>();

    // Map<eventId, sum> - таблица сумм векторов для каждого события
    private final Map<Long, BigDecimal> eventSums = new HashMap<>();

    // Map<eventId1, Map<eventId2, sum>> - таблица сумм минимумов пар векторов событий
    private final Map<Long, Map<Long, BigDecimal>> minWeightSums = new HashMap<>();

    public void handleUserAction(UserActionAvro userActionAvro) {
        Long userId = userActionAvro.getUserId();
        Long eventId = userActionAvro.getEventId();
        BigDecimal oldWeight = BigDecimal.ZERO;
        BigDecimal newWeight = customProperties.getAggregator().getWeights().ofUserAction(userActionAvro);
        log.debug("IN: W[{},{}] = {}", userId, eventId, newWeight);

        Map<Long, BigDecimal> userWeights = weightsByUser.computeIfAbsent(userId, id -> new HashMap<>());
        Map<Long, BigDecimal> eventWeights = weightsByEvent.computeIfAbsent(eventId, id -> new HashMap<>());

        if (userWeights.containsKey(eventId)) {
            oldWeight = userWeights.get(eventId);
            if (newWeight.compareTo(oldWeight) <= 0) {
                log.debug("WEIGHT: {} <= {}, ничего не делаем!", newWeight, oldWeight);
                return;
            }
            log.debug("WEIGHT: {} > {}, кладем {}", newWeight, oldWeight, newWeight);
        } else {
            log.debug("WEIGHT: первое взаимодействие юзера {} и события {}, кладем {}", userId, eventId, newWeight);
        }

        // обновляем веса
        userWeights.put(eventId, newWeight);
        eventWeights.put(userId, newWeight);
        // обновляем сумму вектора события
        recountEventSum(eventId, newWeight, oldWeight);
        // обновляем суммы минимумов векторов событий
        if (customProperties.getAggregator().getMinimumSumAlgorithm().toLowerCase().contains("naive")) {
            recountEventMinWeightsNaive(userId, eventId);
        } else {
            recountEventMinWeightsOptimized(userId, eventId, oldWeight, newWeight);
        }
        // считаем подобие и отправляем в кафку
        sendSimilarity(userId, eventId);
    }

    // вычисление и отправка подобия на основе имеющихся таблиц
    private void sendSimilarity(Long userId, Long eventId) {
        for (Long anotherEventId : weightsByUser.get(userId).keySet()) {
            if (!Objects.equals(eventId, anotherEventId)) {
                long first = Math.min(eventId, anotherEventId);
                long second = Math.max(eventId, anotherEventId);

                double numerator = minWeightSums.get(first).get(second).doubleValue();
                double sqrt1 = Math.sqrt(eventSums.get(first).doubleValue());
                double sqrt2 = Math.sqrt(eventSums.get(second).doubleValue());
                double denominator = sqrt1 * sqrt2;
                double similarity = numerator / denominator;

                EventSimilarityAvro eventSimilarityAvro = EventSimilarityAvro.newBuilder()
                        .setEventA(first)
                        .setEventB(second)
                        .setScore(similarity)
                        .setTimestamp(Instant.now())
                        .build();

                kafkaTemplate.send(customProperties.getKafka().getEventsSimilarityTopic(), eventSimilarityAvro);
                log.debug("SENT: {}", eventSimilarityAvro);
            }
        }
    }

    // пересчет таблицы сумм векторов события
    private void recountEventSum(Long eventId, BigDecimal newWeight, BigDecimal oldWeight) {
        BigDecimal delta = newWeight.subtract(oldWeight);
        BigDecimal prevSum = eventSums.get(eventId);
        eventSums.merge(eventId, delta, BigDecimal::add);
        log.debug("SUM: Для события {} пересчитана сумма {} + {} = {}", eventId, prevSum, delta, eventSums.get(eventId));
    }

    // пересчет таблицы сумм минимумов двух векторов событий - версия 1, наивная
    private void recountEventMinWeightsNaive(Long userId, Long eventId) {
        for (Long secondEventId : weightsByUser.get(userId).keySet()) {
            if (!Objects.equals(secondEventId, eventId)) {
                Map<Long, BigDecimal> eventWeights1 = weightsByEvent.get(eventId);
                Map<Long, BigDecimal> eventWeights2 = weightsByEvent.get(secondEventId);

                Set<Long> userIds = new HashSet<>(eventWeights1.keySet());
                userIds.retainAll(eventWeights2.keySet());

                BigDecimal sum = userIds.stream()
                        .map(id -> eventWeights1.get(id).min(eventWeights2.get(id)))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                Long first = Math.min(eventId, secondEventId);
                Long second = Math.max(eventId, secondEventId);
                minWeightSums.computeIfAbsent(first, k -> new HashMap<>()).put(second, sum);;
                log.debug("MIN1: Для событий {} и {} посчитана сумма минимумов {}", first, second, sum);
            }
        }
    }

    // пересчет таблицы сумм минимумов двух векторов событий - версия 2, оптимизированная
    private void recountEventMinWeightsOptimized(Long userId, Long eventId, BigDecimal oldWeight, BigDecimal newWeight) {
        for (Map.Entry<Long, BigDecimal> anotherEventEntry : weightsByUser.get(userId).entrySet()) {
            if (!Objects.equals(eventId, anotherEventEntry.getKey())) {
                Long first = Math.min(eventId, anotherEventEntry.getKey());
                Long second = Math.max(eventId, anotherEventEntry.getKey());

                Map<Long, BigDecimal> firstEventSums = minWeightSums.computeIfAbsent(first, k -> new HashMap<>());
                BigDecimal oldSum = firstEventSums.getOrDefault(second, BigDecimal.ZERO);

                BigDecimal oldMinimum = oldWeight.min(anotherEventEntry.getValue());
                BigDecimal newMinimum = newWeight.min(anotherEventEntry.getValue());
                BigDecimal newSum = oldSum.subtract(oldMinimum).add(newMinimum);

                firstEventSums.put(second, newSum);
                log.debug("MIN2: Для событий {} и {} посчитана сумма минимумов {}", first, second, newSum);
            }
        }
    }

}
