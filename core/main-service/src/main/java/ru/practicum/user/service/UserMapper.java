package ru.practicum.user.service;

import ru.practicum.dto.user.NewUserRequestDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.user.dal.User;

public class UserMapper {

    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public static User toEntity(NewUserRequestDto dto) {
        return User.builder()
                .email(dto.getEmail())
                .name(dto.getName())
                .build();
    }

    public static UserShortDto toUserShortDto(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

}
