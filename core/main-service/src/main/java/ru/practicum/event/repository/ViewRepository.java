package ru.practicum.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.event.model.View;

import java.util.List;

public interface ViewRepository extends JpaRepository<View, Long> {

    long countByEventId(Long eventId);

    @Query("""
            SELECT v.event.id, count(v)
            FROM View v
            WHERE v.event.id IN :eventIds
            GROUP BY v.event.id
            """)
    List<Object[]> countsByEventIds(
            @Param("eventIds") List<Long> eventIds
    );

    boolean existsByEventIdAndIp(Long eventId, String ip);

}
