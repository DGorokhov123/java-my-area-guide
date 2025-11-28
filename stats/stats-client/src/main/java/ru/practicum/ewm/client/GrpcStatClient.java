package ru.practicum.ewm.client;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import ru.practicum.EventHitDto;
import ru.practicum.EventStatsResponseDto;
import ru.practicum.grpc.collector.RecommendationsControllerGrpc;
import ru.practicum.grpc.collector.UserActionControllerGrpc;
import ru.practicum.grpc.similarity.reports.InteractionsCountRequestProto;
import ru.practicum.grpc.similarity.reports.RecommendedEventProto;
import ru.practicum.grpc.similarity.reports.UserPredictionsRequestProto;
import ru.practicum.grpc.user.action.ActionTypeProto;
import ru.practicum.grpc.user.action.UserActionProto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Primary
@Slf4j
@Component
@RequiredArgsConstructor
public class GrpcStatClient implements StatClient {

    private final UserActionControllerGrpc.UserActionControllerBlockingStub userActionStub;
    private final RecommendationsControllerGrpc.RecommendationsControllerBlockingStub recommendationsStub;


    @Override
    public void hit(EventHitDto eventHitDto) {
        throw new UnsupportedOperationException("Method hit() is not supported");
    }

    @Override
    public Collection<EventStatsResponseDto> stats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        throw new UnsupportedOperationException("Method stats() is not supported");
    }

    @Override
    public String sendView(Long userId, Long eventId) {
        return sendAction(userId, eventId, ActionTypeProto.ACTION_VIEW);
    }

    @Override
    public String sendRegister(Long userId, Long eventId) {
        return sendAction(userId, eventId, ActionTypeProto.ACTION_REGISTER);
    }

    @Override
    public String sendLike(Long userId, Long eventId) {
        return sendAction(userId, eventId, ActionTypeProto.ACTION_LIKE);
    }

    @Override
    public Map<Long, Double> getUserRecommendations(Long userId, Integer size) {
        UserPredictionsRequestProto requestProto = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(size)
                .build();
        try {
            Iterator<RecommendedEventProto> recommendations = recommendationsStub.getRecommendationsForUser(requestProto);

            Map<Long, Double> result = StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(recommendations, Spliterator.ORDERED),
                    false
            ).collect(Collectors.toMap(
                    RecommendedEventProto::getEventId,
                    RecommendedEventProto::getScore
            ));
            log.debug("Received {} recommendations for user {}", result.size(), userId);
            return result;
        } catch (Exception e) {
            log.warn("Failed getting User Recommendations by GRPC: {}", e.getMessage());
            return Map.of();
        }
    }

    @Override
    public Map<Long, Double> getRatingsByEventIdList(List<Long> eventIdList) {
        InteractionsCountRequestProto requestProto = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIdList)
                .build();
        try {
            Iterator<RecommendedEventProto> ratingIterator = recommendationsStub.getInteractionsCount(requestProto);

            Map<Long, Double> result = StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(ratingIterator, Spliterator.ORDERED),
                    false
            ).collect(Collectors.toMap(
                    RecommendedEventProto::getEventId,
                    RecommendedEventProto::getScore
            ));
            log.debug("Received {} ratings for {} events", result.size(), eventIdList.size());
            return result;
        } catch (Exception e) {
            log.warn("Failed getting Event Ratings by GRPC: {}", e.getMessage());
            return Map.of();
        }
    }

    private String sendAction(Long userId, Long eventId, ActionTypeProto action) {
        Instant instant = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
        UserActionProto userActionProto = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(action)
                .setTimestamp(timestamp)
                .build();
        try {
            userActionStub.collectUserAction(userActionProto);
            log.debug("Sent Event View action: {}", userActionProto);
            return "true";
        } catch (Exception e) {
            log.warn("Failed sending Event View action by GRPC: {}", e.getMessage());
            return "false";
        }
    }

}
