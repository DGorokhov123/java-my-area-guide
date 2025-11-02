package ru.practicum.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequestDto {

    @NotBlank(message = "Field 'email' shouldn't be blank")
    @Email(message = "Field 'email' should match email mask")
    @Size(min = 6, max = 254, message = "Field 'email' should be from 6 to 254 characters")
    private String email;

    @NotBlank(message = "Field 'name' shouldn't be blank")
    @Size(min = 2, max = 250, message = "Field 'name' should be from 2 to 250 characters")
    private String name;

}
