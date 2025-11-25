package ru.practicum.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {

    EventSimilarity findByEventAAndEventB(Long eventA, Long eventB);

    @Query(nativeQuery = true, value = """
            (
              SELECT s.event_b event_id, s.score score
              FROM similarities s
              WHERE s.event_a IN :eventList
              AND NOT EXISTS (SELECT 1 FROM actions a WHERE a.event_id = s.event_b AND a.user_id = :userId)
            )
            UNION ALL
            (
              SELECT s.event_a event_id, s.score score
              FROM similarities s
              WHERE s.event_b IN :eventList
              AND NOT EXISTS (SELECT 1 FROM actions a WHERE a.event_id = s.event_a AND a.user_id = :userId)
            )
            ORDER BY score DESC
            LIMIT :limit;
            """)
    List<Object[]> findSimilarByEventIdListNotSeenByUser(
            @Param("userId") Long userId,
            @Param("eventList") List<Long> eventList,
            @Param("limit") Integer limit
    );

    @Query(nativeQuery = true, value = """
            WITH simwt AS (
            (
              SELECT s.event_a src_event, s.event_b event_id, s.score score, a.weight weight
              FROM similarities s
              JOIN actions a ON a.event_id = s.event_b AND a.user_id = :userId
              WHERE s.event_a IN :eventList
            )
            UNION ALL
            (
              SELECT s.event_b src_event, s.event_a event_id, s.score score, a.weight weight
              FROM similarities s
              JOIN actions a ON a.event_id = s.event_a AND a.user_id = :userId
              WHERE s.event_b IN :eventList
            ))
            SELECT src_event, SUM(score*weight)/SUM(score) result_score
            FROM simwt
            GROUP BY src_event
            ORDER BY result_score DESC
            """)
    List<Object[]> findWeightedAverageListByEventIdList(
            @Param("userId") Long userId,
            @Param("eventList") List<Long> eventList
    );

}
