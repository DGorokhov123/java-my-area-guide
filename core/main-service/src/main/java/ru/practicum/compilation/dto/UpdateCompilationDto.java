package ru.practicum.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.validation.AtLeastOneNotNull;
import ru.practicum.validation.NotBlankButNullAllowed;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@AtLeastOneNotNull(fields = {"events", "pinned", "title"}, message = "DTO has only null data fields")
public class UpdateCompilationDto {

    @Builder.Default
    private Set<Long> events = new HashSet<>();

    private Boolean pinned;

    @NotBlankButNullAllowed
    @Size(min = 1, max = 50)
    private String title;

}