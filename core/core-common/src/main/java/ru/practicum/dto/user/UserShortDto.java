package ru.practicum.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserShortDto {

    private Long id;

    private String name;

    public static UserShortDto makeDummy(Long id) {
        UserShortDto dto = new UserShortDto();
        dto.setId(id);
        return dto;
    }

}