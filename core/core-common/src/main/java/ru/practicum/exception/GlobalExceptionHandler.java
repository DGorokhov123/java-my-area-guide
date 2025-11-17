package ru.practicum.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // SPRING EXCEPTIONS -----------------------------------------------

    @ExceptionHandler(
            ConstraintViolationException.class              // Custom annotation exceptions
    )
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException e, HttpServletRequest request) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("Invalid input");

        log.debug("VALIDATION FAILED: {}", e.getMessage());

        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Validation Failed")
                .message(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(
            MethodArgumentNotValidException.class            // @Valid annotation exceptions
    )
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMessage = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        Object target = e.getBindingResult().getTarget();
        log.debug("VALIDATION FAILED: {} for {}", errorMessage, target);
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Validation Failed")
                .message(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,                        // wrong arguments like -1
            MethodArgumentTypeMismatchException.class,             // argument type mismatch
            HttpMessageNotReadableException.class,                 // wrong json in request body
            MissingServletRequestParameterException.class          // missing RequestParam
    })
    public ResponseEntity<ApiError> handleIllegalArgument(Throwable e, HttpServletRequest request) {
        log.debug("ILLEGAL ARGUMENT: {}", e.getMessage());
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Illegal Argument")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(
            MissingRequestHeaderException.class                        // missing request header
    )
    public ResponseEntity<ApiError> handleMissingRequestHeaderException(MissingRequestHeaderException e, HttpServletRequest request) {
        log.debug("MISSING HEADER: {}", e.getMessage());
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Missing header")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    // CUSTOM EXCEPTIONS --------------------------------------------------------

    @ExceptionHandler(
            BadRequestException.class                        // custom bad request
    )
    public ResponseEntity<ApiError> handleBadRequestException(BadRequestException e, HttpServletRequest request) {
        log.debug("BAD REQUEST: {}", e.getMessage());
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason(e.getReason())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(
            ConflictException.class                        // custom conflict exception
    )
    public ResponseEntity<ApiError> handleConflictException(ConflictException e, HttpServletRequest request) {
        log.debug("CONFLICT: {}", e.getMessage());
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .reason(e.getReason())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(
            ForbiddenException.class                        // custom forbidden exception
    )
    public ResponseEntity<ApiError> handleForbiddenException(ForbiddenException e, HttpServletRequest request) {
        log.debug("FORBIDDEN: {}", e.getMessage());
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.FORBIDDEN)
                .reason(e.getReason())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(
            NotFoundException.class                        // custom not_found exception
    )
    public ResponseEntity<ApiError> handleNotFoundException(NotFoundException e, HttpServletRequest request) {
        log.debug("NOT FOUND: {}", e.getMessage());
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.NOT_FOUND)
                .reason(e.getReason())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(
            ServiceInteractionException.class                        // custom exception
    )
    public ResponseEntity<ApiError> handleServiceInteractionException(ServiceInteractionException e, HttpServletRequest request) {
        log.debug("SERVICE INTERACTION ERROR: {}", e.getMessage());
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .reason(e.getReason())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.SERVICE_UNAVAILABLE);
    }

}
