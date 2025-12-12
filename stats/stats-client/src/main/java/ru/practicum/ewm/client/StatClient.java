package ru.practicum.ewm.client;

import java.util.List;
import java.util.Map;

public interface StatClient {

    String sendView(Long userId, Long eventId);

    String sendRegister(Long userId, Long eventId);

    String sendLike(Long userId, Long eventId);

    Map<Long, Double> getUserRecommendations(Long userId, Integer size);

    Map<Long, Double> getRatingsByEventIdList(List<Long> eventIdList);

}
