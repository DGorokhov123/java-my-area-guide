package ru.practicum.exception;

public class ConflictException extends RuntimeException {

    private final String reason;

    public ConflictException(String message) {
        super(message);
        this.reason = "Integrity constraint has been violated.";
    }

    public ConflictException(String message, String reason) {
        super(message);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

}
