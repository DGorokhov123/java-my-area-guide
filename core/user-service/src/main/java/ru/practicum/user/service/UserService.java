package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.user.NewUserRequestDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dal.User;
import ru.practicum.user.dal.UserRepository;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    // MODIFY OPS

    @Transactional(readOnly = false)
    public UserDto create(NewUserRequestDto newUserRequestDto) {
        if (userRepository.existsByEmail(newUserRequestDto.getEmail())) {
            throw new ConflictException("User with email " + newUserRequestDto.getEmail() + " already exists",
                    "Integrity constraint has been violated");
        }
        User newUser = UserMapper.toNewEntity(newUserRequestDto);
        userRepository.save(newUser);
        return UserMapper.toDto(newUser);
    }

    @Transactional(readOnly = false)
    public void delete(Long userId) {
        User userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        userRepository.delete(userToDelete);
    }

    // GET + HEAD

    public UserDto get(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found User " + userId));
        return UserMapper.toDto(user);
    }

    public UserShortDto getShort(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found User " + userId));
        return UserMapper.toUserShortDto(user);
    }

    // GET COLLECTION

    public List<UserDto> findByIdListWithOffsetAndLimit(List<Long> idList, Integer from, Integer size) {
        if (idList == null || idList.isEmpty()) {
            Sort sort = Sort.by(Sort.Direction.ASC, "id");
            return userRepository.findAll(PageRequest.of(from / size, size, sort))
                    .stream()
                    .map(UserMapper::toDto)
                    .toList();
        } else {
            return userRepository.findAllById(idList)
                    .stream()
                    .map(UserMapper::toDto)
                    .toList();
        }
    }

    public Collection<UserShortDto> findUserShortDtoListByIds(Collection<Long> idList) {
        if (idList == null || idList.isEmpty()) return List.of();
        return userRepository.findAllById(idList).stream()
                .map(UserMapper::toUserShortDto)
                .toList();
    }

    public Collection<UserDto> findUserDtoListByIds(Collection<Long> idList) {
        if (idList == null || idList.isEmpty()) return List.of();
        return userRepository.findAllById(idList).stream()
                .map(UserMapper::toDto)
                .toList();
    }

}
