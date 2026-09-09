package event_registration.exception;


import event_registration.dto.response.ErrorResponse;
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

}
