package ru.practicum.compilation;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.event.model.Event;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "compilations")
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "pinned")
    Boolean pinned;

    @Column(name = "title")
    @Size(min = 1, max = 50)
    @NotEmpty
    private String title;

    @ManyToMany
    @JoinTable(name = "compilations_events",
            joinColumns = @JoinColumn(name = "compilations_id"),
            inverseJoinColumns = @JoinColumn(name = "events_id"))
    Set<Event> events;

    @Override
    public String toString() {
        return "Compilations{" +
                "id=" + id +
                ", pinned=" + pinned +
                ", title='" + title + '\'' +
                ", events=" + events +
                '}';
    }
}

