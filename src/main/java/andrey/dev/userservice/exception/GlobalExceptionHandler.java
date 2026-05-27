package andrey.dev.userservice.exception;

import andrey.dev.userservice.exception.exceptions.*;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(Exception ex, WebRequest request) {
        ErrorResponse error = createErrorResponse(ex, request);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({UserCreatingException.class, UserUpdateException.class})
    public ResponseEntity<ErrorResponse> handleUserBadRequestExceptions(Exception ex, WebRequest request) {
        ErrorResponse error = createErrorResponse(ex, request);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PaymentCardNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentCardNotFoundException(Exception ex, WebRequest request) {
        ErrorResponse error = createErrorResponse(ex, request);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(Exception ex, WebRequest request) {
        ErrorResponse error = createErrorResponse(ex, request);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler({ExpiredJwtException.class, UnsupportedJwtException.class, MalformedJwtException.class})
    public ResponseEntity<ErrorResponse> handleJwtException(Exception ex, WebRequest request) {
        ErrorResponse error = createErrorResponse(ex, request);
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurityException(Exception ex, WebRequest request) {
        ErrorResponse error = createErrorResponse(ex, request);
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(Exception ex, WebRequest request) {
        ErrorResponse error = createErrorResponse(ex, request);
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(Exception ex, WebRequest request) {
        ErrorResponse error = createErrorResponse(ex, request);
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(Exception ex, WebRequest request) {
        ErrorResponse error = createErrorResponse(ex, request);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({PaymentCardsCountException.class, PaymentCardUpdateException.class, PaymentCardCreatingException.class})
    public ResponseEntity<ErrorResponse> handlePaymentCardBadRequestExceptions(Exception ex, WebRequest request) {
        ErrorResponse error = createErrorResponse(ex, request);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex, WebRequest request) {
        log.error("Database error occurred: {}", ex.getMessage(), ex);
        ErrorResponse error = ErrorResponse.builder()
                .path(request.getDescription(false))
                .message("Database error occurred: " + ex.getMostSpecificCause().getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknownExceptions(Exception ex, WebRequest request) {
        ErrorResponse error = createErrorResponse(ex, request);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ErrorResponse createErrorResponse(Exception ex, WebRequest request) {
        log.error("Exception occurred: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
        return ErrorResponse.builder()
                .path(request.getDescription(false))
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

}
