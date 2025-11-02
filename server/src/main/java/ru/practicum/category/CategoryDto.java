package ru.practicum.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.validation.CreateOrUpdateValidator;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    private Long id;

    @NotBlank(groups = {CreateOrUpdateValidator.Create.class, CreateOrUpdateValidator.Update.class})
    @Size(min = 1, max = 50, groups = {CreateOrUpdateValidator.Create.class, CreateOrUpdateValidator.Update.class})
    private String name;
}