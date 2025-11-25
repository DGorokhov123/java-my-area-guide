package ru.practicum.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Objects;

public interface UserActionRepository extends JpaRepository<UserAction, Long> {

    @Query(nativeQuery = true, value = """
            SELECT a.event_id
            FROM actions a
            WHERE a.user_id = :userId
            GROUP BY a.event_id
            ORDER BY MAX(a.timestamp) DESC
            LIMIT :limit
            """)
    List<Long> findRecentEventIdListByUserId(
            @Param("userId") Long userId,
            @Param("limit") Integer limit
    );

    @Query(nativeQuery = true, value = """
            WITH max_weights AS
            (
              SELECT a.event_id event_id, a.user_id user_id, MAX(a.weight) max_weight
              FROM actions a
              WHERE a.event_id IN :eventList
              GROUP BY a.event_id, a.user_id
            )
            SELECT m.event_id event_id, SUM(m.max_weight) weight_sum
            FROM max_weights m
            GROUP BY m.event_id
            """)
    List<Object[]> findWeightSumListByEventIdList(
            @Param("eventList") List<Long> eventList
    );


}
