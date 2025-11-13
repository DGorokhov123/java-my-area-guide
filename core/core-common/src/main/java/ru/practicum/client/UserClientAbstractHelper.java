package ru.practicum.client;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.api.user.UserApi;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ServiceInteractionException;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public abstract class UserClientAbstractHelper {

    protected final UserApi userApiClient;

    // UserShortDto

    public UserShortDto retrieveUserShortDtoByUserIdOrFall(Long userId) {
        try {
            return userApiClient.getUserShort(userId);
        } catch (RuntimeException e) {
            if (isNotFoundCode(e)) throw new NotFoundException("Not found User " + userId);

            log.warn("Service Interaction Error: caught " + e.getClass().getSimpleName() + " - " + e.getMessage());
            throw new ServiceInteractionException("Unable to check User " + userId, "user-service is unavailable");
        }
    }

    public UserShortDto retrieveUserShortDtoByUserId(Long userId) {
        try {
            return userApiClient.getUserShort(userId);
        } catch (RuntimeException e) {
            if (isNotFoundCode(e)) throw new NotFoundException("Not found User " + userId);

            log.warn("Service Interaction Error: caught " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return UserShortDto.makeDummy(userId);
        }
    }

    public Map<Long, UserShortDto> retrieveUserShortDtoMapByUserIdList(Collection<Long> userIdList) {
        try {
            return userApiClient.getUserShortDtoListByIds(userIdList).stream()
                    .collect(Collectors.toMap(UserShortDto::getId, u -> u));
        } catch (RuntimeException e) {
            log.warn("Service Interaction Error: caught " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return userIdList.stream()
                    .collect(Collectors.toMap(id -> id, UserShortDto::makeDummy));
        }
    }

    // UserDto

    public UserDto retrieveUserDtoByUserIdOrFall(Long userId) {
        try {
            return userApiClient.getUser(userId);
        } catch (RuntimeException e) {
            if (isNotFoundCode(e)) throw new NotFoundException("Not found User " + userId);

            log.warn("Service Interaction Error: caught " + e.getClass().getSimpleName() + " - " + e.getMessage());
            throw new ServiceInteractionException("Unable to check User " + userId, "user-service is unavailable");
        }
    }

    public UserDto retrieveUserDtoByUserId(Long userId) {
        try {
            return userApiClient.getUser(userId);
        } catch (RuntimeException e) {
            if (isNotFoundCode(e)) throw new NotFoundException("Not found User " + userId);

            log.warn("Service Interaction Error: caught " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return UserDto.makeDummy(userId);
        }
    }

    public Map<Long, UserDto> retrieveUserDtoMapByUserIdList(Collection<Long> userIdList) {
        try {
            return userApiClient.getUserDtoListByIds(userIdList).stream()
                    .collect(Collectors.toMap(UserDto::getId, u -> u));
        } catch (RuntimeException e) {
            log.warn("Service Interaction Error: caught " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return userIdList.stream()
                    .collect(Collectors.toMap(id -> id, UserDto::makeDummy));
        }
    }

    // PRIVATE METHODS

    private boolean isNotFoundCode(RuntimeException e) {
        if (e instanceof FeignException.NotFound) return true;
        if (e.getCause() != null && e.getCause() instanceof FeignException.NotFound) return true;
        return false;
    }

}