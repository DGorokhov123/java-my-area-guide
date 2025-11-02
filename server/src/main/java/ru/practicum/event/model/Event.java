package ru.practicum.event.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.category.Category;
import ru.practicum.comment.Comment;
import ru.practicum.event.dto.State;
import ru.practicum.request.Request;
import ru.practicum.user.User;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @ManyToOne
    @JoinColumn(name = "initiator", nullable = false)
    User initiator;

    @ManyToOne
    @JoinColumn(name = "categories_id", nullable = false)
    Category category;

    @Column(name = "title", length = 120, nullable = false)
    String title;

    @Column(name = "annotation", length = 2000, nullable = false)
    String annotation;

    @Column(name = "description", length = 7000, nullable = false)
    String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 20, nullable = false)
    State state;

    @Embedded
    Location location;

    @Column(name = "participant_limit", nullable = false)
    Long participantLimit;

    @Column(name = "request_moderation", nullable = false)
    Boolean requestModeration;

    @Column(name = "paid", nullable = false)
    Boolean paid;

    @Column(name = "event_date", nullable = false)
    LocalDateTime eventDate;

    @Column(name = "published_on")
    LocalDateTime publishedOn;

    @Column(name = "created_on", nullable = false)
    LocalDateTime createdOn;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    private List<Request> requests;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    private List<Comment> comments;

}
