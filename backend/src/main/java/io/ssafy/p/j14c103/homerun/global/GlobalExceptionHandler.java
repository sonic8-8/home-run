package io.ssafy.p.j14c103.homerun.global;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HomerunException.class)
    public ResponseEntity<ErrorResponse> handleHomerunException(
            HomerunException exception
    ) {
        ErrorCode errorCode = exception.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.from(errorCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ) {
        List<ValidationError> errors = exception.getBindingResult().getFieldErrors()
                .stream()
                .map(this::toValidationError)
                .toList();

        if (errors.isEmpty()) {
            return badRequest();
        }

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(ErrorResponse.validation(
                        ErrorCode.INVALID_INPUT_VALUE.getCode(),
                        ErrorCode.INVALID_INPUT_VALUE.getMessage(),
                        errors
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.from(errorCode));
    }

    private ResponseEntity<ErrorResponse> badRequest() {
        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(ErrorResponse.of(
                    ErrorCode.INVALID_INPUT_VALUE.getCode(),
                    ErrorCode.INVALID_INPUT_VALUE.getMessage()
                ));
    }

    private ValidationError toValidationError(FieldError fieldError) {
        return ValidationError.of(
                fieldError.getField(),
                fieldError.getDefaultMessage()
        );
    }
}
