package ru.practicum.compilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewCompilationDto {

    @Builder.Default
    Set<Long> events = new HashSet<>();

    @Builder.Default
    Boolean pinned = false;

    @NotBlank
    @Size(min = 1, max = 50)
    String title;

}