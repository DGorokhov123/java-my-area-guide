package ru.practicum.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.NewUserRequestDto;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.user.service.UserService;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Disabled("Выполнять только при запущенных Discovery and Config servers")
@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    // CREATE USER TESTS

    @Test
    void create_WithValidData_ShouldSaveUserAndReturnDto() {
        // Given
        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email("test@example.com")
                .name("Test User")
                .build();

        // When
        UserDto result = userService.create(requestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getName()).isEqualTo("Test User");

        // Verify user was saved in database
        User savedUser = userRepository.findById(result.getId()).orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getName()).isEqualTo("Test User");
    }

    @Test
    void create_WithDuplicateEmail_ShouldThrowConflictException() {
        // Given
        User existingUser = User.builder()
                .email("duplicate@example.com")
                .name("Existing User")
                .build();
        userRepository.save(existingUser);

        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email("duplicate@example.com")
                .name("New User")
                .build();

        // When & Then
        assertThatThrownBy(() -> userService.create(requestDto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("duplicate@example.com");

        // Verify only one user exists
        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getName()).isEqualTo("Existing User");
    }

    @Test
    void create_WithCaseInsensitiveEmail_ShouldWorkCorrectly() {
        // Given
        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email("TeSt@ExAmPLE.COM")
                .name("Test User")
                .build();

        // When
        UserDto result = userService.create(requestDto);

        // Then
        assertThat(result.getEmail()).isEqualTo("TeSt@ExAmPLE.COM");

        // Verify saved in database
        User savedUser = userRepository.findById(result.getId()).orElse(null);
        assertThat(savedUser.getEmail()).isEqualTo("TeSt@ExAmPLE.COM");
    }

    @Test
    void create_WithMinimalValidData_ShouldCreateUser() {
        // Given
        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email("ab@c.de") // 6 characters - minimum valid
                .name("AB") // 2 characters - minimum valid
                .build();

        // When
        UserDto result = userService.create(requestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getEmail()).isEqualTo("ab@c.de");
        assertThat(result.getName()).isEqualTo("AB");
    }

    @Test
    void create_WithMaximalValidData_ShouldCreateUser() {
        // Given
        String maxEmail = "a".repeat(245) + "@test.com"; // 254 characters - maximum valid
        String maxName = "A".repeat(250); // 250 characters - maximum valid

        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email(maxEmail)
                .name(maxName)
                .build();

        // When
        UserDto result = userService.create(requestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getEmail()).isEqualTo(maxEmail);
        assertThat(result.getName()).isEqualTo(maxName);
    }

    // DELETE USER TESTS

    @Test
    void delete_WithExistingUserId_ShouldDeleteUser() {
        // Given
        User user = User.builder()
                .email("todelete@example.com")
                .name("User To Delete")
                .build();
        User savedUser = userRepository.save(user);
        Long userId = savedUser.getId();

        // When
        userService.delete(userId);

        // Then
        assertThat(userRepository.findById(userId)).isEmpty();
        assertThat(userRepository.count()).isEqualTo(0);
    }

    @Test
    void delete_WithNonExistentUserId_ShouldThrowNotFoundException() {
        // Given
        Long nonExistentId = 999L;

        // When & Then
        assertThatThrownBy(() -> userService.delete(nonExistentId))
                .isInstanceOf(NotFoundException.class);

        // Verify database is unchanged
        assertThat(userRepository.count()).isEqualTo(0);
    }

    @Test
    void delete_WithMultipleUsers_ShouldDeleteOnlySpecificUser() {
        // Given
        User user1 = userRepository.save(User.builder()
                .email("user1@example.com")
                .name("User 1")
                .build());
        User user2 = userRepository.save(User.builder()
                .email("user2@example.com")
                .name("User 2")
                .build());
        User user3 = userRepository.save(User.builder()
                .email("user3@example.com")
                .name("User 3")
                .build());

        // When
        userService.delete(user2.getId());

        // Then
        assertThat(userRepository.findById(user1.getId())).isPresent();
        assertThat(userRepository.findById(user2.getId())).isEmpty();
        assertThat(userRepository.findById(user3.getId())).isPresent();
        assertThat(userRepository.count()).isEqualTo(2);
    }

    // FIND BY ID LIST WITH OFFSET AND LIMIT TESTS

    @Test
    void findByIdListWithOffsetAndLimit_WithNullIdList_ShouldReturnPagedResults() {
        // Given
        createTestUsers(15);

        // When
        List<UserDto> result = userService.findByIdListWithOffsetAndLimit(null, 0, 10);

        // Then
        assertThat(result).hasSize(10);
        assertThat(result).extracting(UserDto::getEmail)
                .contains("user0@example.com", "user1@example.com", "user9@example.com");
    }

    @Test
    void findByIdListWithOffsetAndLimit_WithEmptyIdList_ShouldReturnPagedResults() {
        // Given
        createTestUsers(5);

        // When
        List<UserDto> result = userService.findByIdListWithOffsetAndLimit(List.of(), 0, 10);

        // Then
        assertThat(result).hasSize(5);
    }

    @Test
    void findByIdListWithOffsetAndLimit_WithOffsetAndLimit_ShouldReturnCorrectPage() {
        // Given
        createTestUsers(15);

        // When
        List<UserDto> result = userService.findByIdListWithOffsetAndLimit(null, 1, 5);

        // Then
        assertThat(result).hasSize(5);
        assertThat(result).extracting(UserDto::getEmail)
                .contains("user0@example.com", "user1@example.com", "user4@example.com");
    }

    @Test
    void findByIdListWithOffsetAndLimit_WithSpecificIds_ShouldReturnFilteredResults() {
        // Given
        List<User> users = createTestUsers(10);
        List<Long> specificIds = Arrays.asList(
                users.get(0).getId(),
                users.get(2).getId(),
                users.get(4).getId()
        );

        // When
        List<UserDto> result = userService.findByIdListWithOffsetAndLimit(specificIds, 0, 10);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(UserDto::getId)
                .containsExactlyInAnyOrderElementsOf(specificIds);
    }

    @Test
    void findByIdListWithOffsetAndLimit_WithNonExistentIds_ShouldReturnEmptyList() {
        // Given
        createTestUsers(5);
        List<Long> nonExistentIds = Arrays.asList(999L, 1000L, 1001L);

        // When
        List<UserDto> result = userService.findByIdListWithOffsetAndLimit(nonExistentIds, 0, 10);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByIdListWithOffsetAndLimit_WithMixedExistentAndNonExistentIds_ShouldReturnOnlyExistentUsers() {
        // Given
        List<User> users = createTestUsers(3);
        List<Long> mixedIds = Arrays.asList(
                users.get(0).getId(),
                999L, // non-existent
                users.get(1).getId(),
                1000L // non-existent
        );

        // When
        List<UserDto> result = userService.findByIdListWithOffsetAndLimit(mixedIds, 0, 10);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserDto::getId)
                .containsExactlyInAnyOrder(users.get(0).getId(), users.get(1).getId());
    }

    @Test
    void findByIdListWithOffsetAndLimit_WithEmptyDatabase_ShouldReturnEmptyList() {
        // Given - empty database

        // When
        List<UserDto> result = userService.findByIdListWithOffsetAndLimit(null, 0, 10);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByIdListWithOffsetAndLimit_WithLargeOffset_ShouldReturnEmptyList() {
        // Given
        createTestUsers(5);

        // When
        List<UserDto> result = userService.findByIdListWithOffsetAndLimit(null, 10, 10);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByIdListWithOffsetAndLimit_WithSingleUser_ShouldReturnSingleResult() {
        // Given
        User user = userRepository.save(User.builder()
                .email("single@example.com")
                .name("Single User")
                .build());

        // When
        List<UserDto> result = userService.findByIdListWithOffsetAndLimit(null, 0, 10);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(user.getId());
        assertThat(result.get(0).getEmail()).isEqualTo("single@example.com");
        assertThat(result.get(0).getName()).isEqualTo("Single User");
    }

    // Helper methods

    private List<User> createTestUsers(int count) {
        List<User> users = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            User user = User.builder()
                    .email("user" + i + "@example.com")
                    .name("User " + i)
                    .build();
            users.add(userRepository.save(user));
        }
        return users;
    }

}