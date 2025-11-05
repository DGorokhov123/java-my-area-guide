package ru.practicum;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventHitDto {

    @NotNull(message = "Field 'app' should not be null")
    private String app;

    @NotNull(message = "Field 'uri' should not be null")
    private String uri;

    @NotNull(message = "Field 'ip' should not be null")
    private String ip;

    @NotNull(message = "Field 'timestamp' should not be null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

}
