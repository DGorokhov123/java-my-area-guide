package ru.practicum.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    // MODIFY OPS

    @PostMapping("/admin/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(
            @RequestBody @Valid NewUserRequestDto newUserRequestDto
    ) {
        return userService.create(newUserRequestDto);
    }

    @DeleteMapping("/admin/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @PathVariable @Positive(message = "User Id not valid") Long userId
    ) {
        userService.delete(userId);
    }

    // GET COLLECTION

    @GetMapping("/admin/users")
    public Collection<UserDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return userService.findByIdListWithOffsetAndLimit(ids, from, size);
    }


}
