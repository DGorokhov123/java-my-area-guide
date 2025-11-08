package ru.practicum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.controller.UserController;
import ru.practicum.dto.user.NewUserRequestDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.user.service.UserService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled("Выполнять только при запущенных Discovery and Config servers")
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    // CREATE USER TESTS

    @Test
    void createUser_WithValidData_ReturnsCreatedUser() throws Exception {
        // Given
        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email("test@example.com")
                .name("Test User")
                .build();

        UserDto expectedUser = UserDto.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .build();

        when(userService.create(any(NewUserRequestDto.class))).thenReturn(expectedUser);

        // When & Then
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));

        verify(userService).create(any(NewUserRequestDto.class));
    }

    @Test
    void createUser_WithDuplicateEmail_ReturnsConflict() throws Exception {
        // Given
        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email("duplicate@example.com")
                .name("Test User")
                .build();

        when(userService.create(any(NewUserRequestDto.class)))
                .thenThrow(new ConflictException("User with email duplicate@example.com already exists",
                        "Integrity constraint has been violated"));

        // When & Then
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict());

        verify(userService).create(any(NewUserRequestDto.class));
    }

    @Test
    void createUser_WithBlankEmail_ReturnsBadRequest() throws Exception {
        // Given
        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email("")
                .name("Test User")
                .build();

        // When & Then
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any(NewUserRequestDto.class));
    }

    @Test
    void createUser_WithInvalidEmail_ReturnsBadRequest() throws Exception {
        // Given
        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email("invalid-email")
                .name("Test User")
                .build();

        // When & Then
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any(NewUserRequestDto.class));
    }

    @Test
    void createUser_WithTooShortEmail_ReturnsBadRequest() throws Exception {
        // Given
        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email("a@b.c") // 5 characters, minimum is 6
                .name("Test User")
                .build();

        // When & Then
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any(NewUserRequestDto.class));
    }

    @Test
    void createUser_WithTooLongEmail_ReturnsBadRequest() throws Exception {
        // Given
        String longEmail = "a".repeat(250) + "@example.com"; // More than 254 characters
        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email(longEmail)
                .name("Test User")
                .build();

        // When & Then
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any(NewUserRequestDto.class));
    }

    @Test
    void createUser_WithBlankName_ReturnsBadRequest() throws Exception {
        // Given
        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email("test@example.com")
                .name("")
                .build();

        // When & Then
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any(NewUserRequestDto.class));
    }

    @Test
    void createUser_WithTooShortName_ReturnsBadRequest() throws Exception {
        // Given
        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email("test@example.com")
                .name("A") // 1 character, minimum is 2
                .build();

        // When & Then
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any(NewUserRequestDto.class));
    }

    @Test
    void createUser_WithTooLongName_ReturnsBadRequest() throws Exception {
        // Given
        String longName = "A".repeat(251); // More than 250 characters
        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email("test@example.com")
                .name(longName)
                .build();

        // When & Then
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any(NewUserRequestDto.class));
    }

    @Test
    void createUser_WithNullEmail_ReturnsBadRequest() throws Exception {
        // Given
        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email(null)
                .name("Test User")
                .build();

        // When & Then
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any(NewUserRequestDto.class));
    }

    @Test
    void createUser_WithNullName_ReturnsBadRequest() throws Exception {
        // Given
        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email("test@example.com")
                .name(null)
                .build();

        // When & Then
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any(NewUserRequestDto.class));
    }

    // DELETE USER TESTS

    @Test
    void deleteUser_WithValidId_ReturnsNoContent() throws Exception {
        // Given
        Long userId = 1L;
        doNothing().when(userService).delete(userId);

        // When & Then
        mockMvc.perform(delete("/admin/users/{userId}", userId))
                .andExpect(status().isNoContent());

        verify(userService).delete(userId);
    }

    @Test
    void deleteUser_WithNonExistentId_ReturnsNotFound() throws Exception {
        // Given
        Long userId = 999L;
        doThrow(new NotFoundException("User with id=" + userId + " was not found"))
                .when(userService).delete(userId);

        // When & Then
        mockMvc.perform(delete("/admin/users/{userId}", userId))
                .andExpect(status().isNotFound());

        verify(userService).delete(userId);
    }

    @Test
    void deleteUser_WithInvalidId_ReturnsBadRequest() throws Exception {
        // Given
        Long invalidUserId = -1L;

        // When & Then
        mockMvc.perform(delete("/admin/users/{userId}", invalidUserId))
                .andExpect(status().isBadRequest());

        verify(userService, never()).delete(any(Long.class));
    }

    @Test
    void deleteUser_WithZeroId_ReturnsBadRequest() throws Exception {
        // Given
        Long invalidUserId = 0L;

        // When & Then
        mockMvc.perform(delete("/admin/users/{userId}", invalidUserId))
                .andExpect(status().isBadRequest());

        verify(userService, never()).delete(any(Long.class));
    }

    // GET USERS TESTS

    @Test
    void getUsers_WithoutParameters_ReturnsDefaultPagedUsers() throws Exception {
        // Given
        List<UserDto> expectedUsers = Arrays.asList(
                UserDto.builder().id(1L).email("user1@example.com").name("User 1").build(),
                UserDto.builder().id(2L).email("user2@example.com").name("User 2").build()
        );

        when(userService.findByIdListWithOffsetAndLimit(null, 0, 10))
                .thenReturn(expectedUsers);

        // When & Then
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].email").value("user1@example.com"))
                .andExpect(jsonPath("$[0].name").value("User 1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].email").value("user2@example.com"))
                .andExpect(jsonPath("$[1].name").value("User 2"));

        verify(userService).findByIdListWithOffsetAndLimit(null, 0, 10);
    }

    @Test
    void getUsers_WithCustomPagination_ReturnsPagedUsers() throws Exception {
        // Given
        List<UserDto> expectedUsers = Arrays.asList(
                UserDto.builder().id(3L).email("user3@example.com").name("User 3").build()
        );

        when(userService.findByIdListWithOffsetAndLimit(null, 5, 5))
                .thenReturn(expectedUsers);

        // When & Then
        mockMvc.perform(get("/admin/users")
                        .param("from", "5")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3L))
                .andExpect(jsonPath("$[0].email").value("user3@example.com"))
                .andExpect(jsonPath("$[0].name").value("User 3"));

        verify(userService).findByIdListWithOffsetAndLimit(null, 5, 5);
    }

    @Test
    void getUsers_WithSpecificIds_ReturnsFilteredUsers() throws Exception {
        // Given
        List<Long> userIds = Arrays.asList(1L, 3L);
        List<UserDto> expectedUsers = Arrays.asList(
                UserDto.builder().id(1L).email("user1@example.com").name("User 1").build(),
                UserDto.builder().id(3L).email("user3@example.com").name("User 3").build()
        );

        when(userService.findByIdListWithOffsetAndLimit(userIds, 0, 10))
                .thenReturn(expectedUsers);

        // When & Then
        mockMvc.perform(get("/admin/users")
                        .param("ids", "1,3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(3L));

        verify(userService).findByIdListWithOffsetAndLimit(userIds, 0, 10);
    }

    @Test
    void getUsers_WithEmptyResult_ReturnsEmptyList() throws Exception {
        // Given
        when(userService.findByIdListWithOffsetAndLimit(null, 0, 10))
                .thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userService).findByIdListWithOffsetAndLimit(null, 0, 10);
    }

    @Test
    void getUsers_WithSingleId_ReturnsFilteredUser() throws Exception {
        // Given
        List<Long> userIds = Arrays.asList(1L);
        List<UserDto> expectedUsers = Arrays.asList(
                UserDto.builder().id(1L).email("user1@example.com").name("User 1").build()
        );

        when(userService.findByIdListWithOffsetAndLimit(userIds, 0, 10))
                .thenReturn(expectedUsers);

        // When & Then
        mockMvc.perform(get("/admin/users")
                        .param("ids", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].email").value("user1@example.com"))
                .andExpect(jsonPath("$[0].name").value("User 1"));

        verify(userService).findByIdListWithOffsetAndLimit(userIds, 0, 10);
    }

    @Test
    void getUsers_WithNonExistentIds_ReturnsEmptyList() throws Exception {
        // Given
        List<Long> userIds = Arrays.asList(999L, 1000L);

        when(userService.findByIdListWithOffsetAndLimit(userIds, 0, 10))
                .thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/admin/users")
                        .param("ids", "999,1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userService).findByIdListWithOffsetAndLimit(userIds, 0, 10);
    }

}