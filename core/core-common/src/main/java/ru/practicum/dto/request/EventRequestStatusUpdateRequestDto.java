package ru.practicum.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequestDto {

    @NotEmpty(message = "Field 'requestIds' shouldn't be empty")
    private List<Long> requestIds;

    @NotNull(message = "Field 'status' shouldn't be null")
    private ParticipationRequestStatus status;

}
