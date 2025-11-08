package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewEventDto {

    @NotNull
    @Positive
    private Long category;

    @NotBlank
    @Size(min = 3, max = 120)
    private String title;

    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;

    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;

    private LocationDto location;

    private Boolean requestModeration = true;

    private Boolean paid = false;

    @PositiveOrZero
    private Long participantLimit = 0L;

    @NotNull
    @Future(message = "Event should be in future")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

}
