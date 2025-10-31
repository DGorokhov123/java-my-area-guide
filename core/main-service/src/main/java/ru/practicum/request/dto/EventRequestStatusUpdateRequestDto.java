package ru.practicum.request.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.request.ParticipationRequestStatus;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequestDto {

    @NotEmpty(message = "Field 'requestIds' shouldn't be empty")
    private List<Long> requestIds;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Field 'status' shouldn't be null")
    private ParticipationRequestStatus status;

}
