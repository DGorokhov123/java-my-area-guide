package ru.practicum.compilation.dal;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.practicum.event.dal.Event;

import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "compilations")
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id")
    private Long id;

    @Column(name = "pinned", nullable = false)
    private Boolean pinned;

    @Size(min = 1, max = 50)
    @NotEmpty
    @Column(name = "title", length = 50, nullable = false)
    private String title;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "compilations_events",
            joinColumns = @JoinColumn(name = "compilations_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "events_id", nullable = false))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<Event> events;

}