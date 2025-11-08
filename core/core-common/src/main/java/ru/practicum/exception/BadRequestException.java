package ru.practicum.exception;

public class BadRequestException extends RuntimeException {

    private final String reason;

    public BadRequestException(String message) {
        super(message);
        this.reason = "Incorrectly made request.";
    }

    public BadRequestException(String message, String reason) {
        super(message);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

}
