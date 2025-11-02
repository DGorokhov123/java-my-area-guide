package ru.practicum.exception;

public class NotFoundException extends RuntimeException {

    private final String reason;

    public NotFoundException(String message) {
        super(message);
        this.reason = "The required object was not found.";
    }

    public NotFoundException(String message, String reason) {
        super(message);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

}
