package ru.practicum.exception;

import lombok.Getter;

@Getter
public class ForbiddenException extends RuntimeException {

    private final String reason;

    public ForbiddenException(String message) {
        super(message);
        this.reason = "For the requested operation the conditions are not met.";
    }

    public ForbiddenException(String message, String reason) {
        super(message);
        this.reason = reason;
    }

}