package ru.practicum.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;

    private String email;

    private String name;

    public static UserDto makeDummy(Long id) {
        UserDto dto = new UserDto();
        dto.setId(id);
        return dto;
    }

}
