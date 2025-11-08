package ru.practicum.api.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.user.NewUserRequestDto;
import ru.practicum.dto.user.UserDto;

import java.util.Collection;
import java.util.List;

public interface UserApi {

    // MODIFY OPS

    @PostMapping("/admin/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(
            @RequestBody @Valid NewUserRequestDto newUserRequestDto
    );

    @DeleteMapping("/admin/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @PathVariable @Positive(message = "User Id not valid") Long userId
    );

    // GET COLLECTION

    @GetMapping("/admin/users")
    @ResponseStatus(HttpStatus.OK)
    public Collection<UserDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    );

}