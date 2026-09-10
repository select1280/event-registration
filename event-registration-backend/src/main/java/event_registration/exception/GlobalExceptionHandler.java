package event_registration.exception;


import event_registration.dto.response.ErrorResponse;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException exception
    ){
        ErrorResponse response = new ErrorResponse(
                "BUSINESS_RULE_VIOLATION",
                exception.getMessage(),
                List.of()
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception
    ){
        List<String> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map( error -> error.getField() + ": " + error.getDefaultMessage()
                )
                .sorted()
                .toList();

        ErrorResponse response = new ErrorResponse(
                "VALIDATION_FAILED",
                "輸入資料驗證失敗",
                details
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException exception
    ){
        ErrorResponse response = new ErrorResponse(
                "RESOURCE_NOT_FOUND",
                exception.getMessage(),
                List.of()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception
    ){
        Throwable cause = exception;

        while (cause != null){
            if(cause instanceof ConstraintViolationException violation && "uk_members_email".equals(violation.getConstraintName())
            ){
                ErrorResponse response = new ErrorResponse(
                        "BUSINESS_RULE_VIOLATION",
                        "此 Email 已被註冊",
                        List.of()
                );

                return ResponseEntity.badRequest().body(response);
            }

            cause = cause.getCause();
        }

        throw exception;
    }

}
