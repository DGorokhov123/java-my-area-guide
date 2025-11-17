package ru.practicum.api.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.user.NewUserRequestDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;

import java.util.Collection;
import java.util.List;

public interface UserApi {

    // MODIFY OPS

    @PostMapping("/admin/users")
    @ResponseStatus(HttpStatus.CREATED)
    UserDto createUser(
            @RequestBody @Valid NewUserRequestDto newUserRequestDto
    );

    @DeleteMapping("/admin/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteUser(
            @PathVariable @Positive(message = "User Id not valid") Long userId
    );

    // GET + HEAD

    @GetMapping("/admin/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    UserDto getUser(
            @PathVariable @Positive(message = "User Id not valid") Long userId
    );

    @GetMapping("/admin/users/{userId}/short")
    @ResponseStatus(HttpStatus.OK)
    UserShortDto getUserShort(
            @PathVariable @Positive(message = "User Id not valid") Long userId
    );

    // GET COLLECTION

    @GetMapping("/admin/users")
    @ResponseStatus(HttpStatus.OK)
    Collection<UserDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    );

    @GetMapping("/admin/users/all/short")
    @ResponseStatus(HttpStatus.OK)
    Collection<UserShortDto> getUserShortDtoListByIds(
            @RequestParam(required = false) Collection<Long> ids
    );

    @GetMapping("/admin/users/all/full")
    @ResponseStatus(HttpStatus.OK)
    Collection<UserDto> getUserDtoListByIds(
            @RequestParam(required = false) Collection<Long> ids
    );

}