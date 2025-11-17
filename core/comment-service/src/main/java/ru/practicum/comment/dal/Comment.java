package ru.practicum.comment.dal;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments", indexes = {
        @Index(name = "idx_comments_event_id", columnList = "event_id"),
        @Index(name = "idx_comments_textual_content", columnList = "textual_content")
})
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "textual_content", length = 1000, nullable = false)
    private String text;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "patch_time")
    private LocalDateTime patchTime;

    @Column(name = "approved", nullable = false)
    private Boolean approved;

}