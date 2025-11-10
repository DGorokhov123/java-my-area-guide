package ru.practicum.request.dal;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.practicum.dto.request.ParticipationRequestStatus;
import ru.practicum.event.dal.Event;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "requests", indexes = {
        @Index(name = "idx_requests_requester_id", columnList = "requester_id"),
        @Index(name = "idx_requests_event_id", columnList = "event_id")
})

public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id")
    private Long id;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 15, nullable = false)
    private ParticipationRequestStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime created;

}
