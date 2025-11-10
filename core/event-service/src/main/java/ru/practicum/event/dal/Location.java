package ru.practicum.event.dal;

import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Location {

    private Float lat;

    private Float lon;

}
