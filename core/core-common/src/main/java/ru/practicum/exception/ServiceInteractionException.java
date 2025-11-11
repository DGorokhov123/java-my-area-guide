package ru.practicum.exception;

import lombok.Getter;

@Getter
public class ServiceInteractionException extends RuntimeException {

    private final String reason;

    public ServiceInteractionException(String message) {
        super(message);
        this.reason = "Unable to get info from microservice";
    }

    public ServiceInteractionException(String message, String reason) {
        super(message);
        this.reason = reason;
    }

}