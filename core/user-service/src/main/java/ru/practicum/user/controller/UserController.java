package ru.practicum.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.user.UserApi;
import ru.practicum.dto.user.NewUserRequestDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.user.service.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class UserController implements UserApi {

    private final UserService userService;

    // MODIFY OPS

    @Override
    public UserDto createUser(NewUserRequestDto newUserRequestDto) {
        return userService.create(newUserRequestDto);
    }

    @Override
    public void deleteUser(Long userId) {
        userService.delete(userId);
    }

    // GET + HEAD

    @Override
    public UserDto getUser(Long userId) {
        return userService.get(userId);
    }

    @Override
    public UserShortDto getUserShort(Long userId) {
        return userService.getShort(userId);
    }

    @Override
    public void checkUser(Long userId) {
        userService.check(userId);
    }

    // GET COLLECTION

    @Override
    public Collection<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        return userService.findByIdListWithOffsetAndLimit(ids, from, size);
    }

    @Override
    public Collection<UserShortDto> getUserShortDtoListByIds(Collection<Long> ids) {
        return userService.findUserShortDtoListByIds(ids);
    }

    @Override
    public Collection<UserDto> getUserDtoListByIds(Collection<Long> ids) {
        return userService.findUserDtoListByIds(ids);
    }

}