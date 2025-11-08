package ru.practicum.event.dal;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.practicum.category.dal.Category;
import ru.practicum.comment.dal.Comment;
import ru.practicum.dto.event.State;
import ru.practicum.request.dal.Request;
import ru.practicum.user.dal.User;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "events", indexes = {
        @Index(name = "idx_events_initiator", columnList = "initiator"),
        @Index(name = "idx_events_categories_id", columnList = "categories_id")
})
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "initiator", nullable = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private User initiator;

    @ManyToOne
    @JoinColumn(name = "categories_id", nullable = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private Category category;

    @Column(name = "title", length = 120, nullable = false)
    private String title;

    @Column(name = "annotation", length = 2000, nullable = false)
    private String annotation;

    @Column(name = "description", length = 7000, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 20, nullable = false)
    private State state;

    @Embedded
    private Location location;

    @Column(name = "participant_limit", nullable = false)
    private Long participantLimit;

    @Column(name = "request_moderation", nullable = false)
    private Boolean requestModeration;

    @Column(name = "paid", nullable = false)
    private Boolean paid;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    private List<Request> requests;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    private List<Comment> comments;

}
