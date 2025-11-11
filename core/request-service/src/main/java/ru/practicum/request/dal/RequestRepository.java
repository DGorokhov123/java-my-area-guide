package ru.practicum.request.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.dto.request.ParticipationRequestStatus;

import java.util.Collection;
import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

    long countByEventIdAndStatus(Long eventId, ParticipationRequestStatus status);

    List<Request> findByRequesterId(Long userId);

    List<Request> findByEventId(Long eventId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Request r SET r.status = :status WHERE r.id IN :ids")
    void updateStatusByIds(
            @Param("ids") List<Long> ids,
            @Param("status") ParticipationRequestStatus status
    );

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Request r
            SET r.status = 'REJECTED'
            WHERE r.eventId = :eventId
            AND r.status = 'PENDING'
            """)
    void setStatusToRejectForAllPending(
            @Param("eventId") Long eventId
    );

    @Query("""
            SELECT r.eventId, count(r)
            FROM Request r
            WHERE r.eventId IN :eventIds
            GROUP BY r.eventId
            """)
    List<Object[]> getConfirmedRequestsByEventIds(
            @Param("eventIds") Collection<Long> eventIds
    );

}
