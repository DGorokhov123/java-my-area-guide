package ru.practicum.compilation;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.validation.AtLeastOneNotNull;
import ru.practicum.validation.NotBlankButNullAllowed;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@AtLeastOneNotNull(fields = {"events", "pinned", "title"}, message = "DTO has only null data fields")
public class UpdateCompilationDto {

    @Builder.Default
    Set<Long> events = new HashSet<>();

    Boolean pinned;

    @NotBlankButNullAllowed
    @Size(min = 1, max = 50)
    String title;

}