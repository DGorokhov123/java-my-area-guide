package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dal.EventSimilarityRepository;
import ru.practicum.dal.UserActionRepository;
import ru.practicum.grpc.similarity.reports.InteractionsCountRequestProto;
import ru.practicum.grpc.similarity.reports.RecommendedEventProto;
import ru.practicum.grpc.similarity.reports.SimilarEventsRequestProto;
import ru.practicum.grpc.similarity.reports.UserPredictionsRequestProto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimilarityReportService {

    private final UserActionRepository userActionRepository;
    private final EventSimilarityRepository eventSimilarityRepository;

    // поток рекомендованных мероприятий для указанного пользователя
    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        long userId = request.getUserId();
        int maxResults = request.getMaxResults();

        // Выгрузить мероприятия, с которыми пользователь уже взаимодействовал, от новых к старым, первые N
        List<Long> recentUserEventIds = userActionRepository.findRecentEventIdListByUserId(userId, maxResults);

        // Найти мероприятия, похожие на те, что отобрали, но при этом пользователь с ними не взаимодействовал
        // сортировать по коэффициенту подобия от большего к меньшему. Выбрать первые N
        List<Long> similarEventIds = eventSimilarityRepository.findSimilarByEventIdListNotSeenByUser(
                        userId,
                        recentUserEventIds,
                        maxResults
                ).stream()
                .map(o -> ((Number) o[0]).longValue())
                .toList();

        // найдем средневзвешенную оценку для каждого полученного ивента
        List<Object[]> averageResult = eventSimilarityRepository.findWeightedAverageListByEventIdList(userId, similarEventIds);
        return averageResult.stream()
                .map(o -> RecommendedEventProto.newBuilder()
                        .setEventId(((Number) o[0]).longValue())
                        .setScore(((Number) o[1]).doubleValue())
                        .build())
                .toList();
    }

    // поток мероприятий, похожих на заданное, с которыми пользователь не взаимодействовал
    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        long eventId = request.getEventId();
        long userId = request.getUserId();
        int maxResults = request.getMaxResults();

        return eventSimilarityRepository.findSimilarByEventIdListNotSeenByUser(
                        userId,
                        List.of(eventId),
                        maxResults
                ).stream()
                .map(o -> RecommendedEventProto.newBuilder()
                        .setEventId(((Number) o[0]).longValue())
                        .setScore(((Number) o[1]).doubleValue())
                        .build())
                .toList();
    }

    // поток мероприятий с суммой максимальных весов действий всех пользователей с этими мероприятиями
    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        List<Long> eventIdList = request.getEventIdList();
        Map<Long, Double> sumMap = userActionRepository.findWeightSumListByEventIdList(eventIdList)
                .stream()
                .collect(Collectors.toMap(
                        o -> ((Number) o[0]).longValue(),
                        o -> ((Number) o[1]).doubleValue()
                ));

        return eventIdList.stream()
                .map(id -> RecommendedEventProto.newBuilder()
                        .setEventId(id)
                        .setScore(sumMap.computeIfAbsent(id, k -> 0.0))
                        .build())
                .toList();
    }

}
