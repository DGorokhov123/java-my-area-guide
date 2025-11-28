package ru.practicum.dal;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.time.Instant;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "similarities",
        indexes = {@Index(name = "idx_similarities_event_a_event_b", columnList = "event_a, event_b")},
        uniqueConstraints = {@UniqueConstraint(name = "unique_event_a_event_b", columnNames = {"event_a", "event_b"})}
)
@Check(constraints = "event_a < event_b")
public class EventSimilarity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id")
    private Long id;

    @Column(name = "event_a", nullable = false)
    private Long eventA;

    @Column(name = "event_b", nullable = false)
    private Long eventB;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

}
