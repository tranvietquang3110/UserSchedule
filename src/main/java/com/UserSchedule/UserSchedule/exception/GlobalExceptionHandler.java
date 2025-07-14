package com.UserSchedule.UserSchedule.exception;

import com.UserSchedule.UserSchedule.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {
    final static String MIN_ATTRIBUTE = "min";
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingAppException(AppException appException) {
        ApiResponse apiResponse = new ApiResponse();
        ErrorCode errorCode = appException.getErrorCode();
        apiResponse.setStatusCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        return  ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }
    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ApiResponse> handlingRuntimeException(RuntimeException runtimeException) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatusCode(ErrorCode.UNCATEGORIZED.getCode());
        apiResponse.setMessage(runtimeException.getMessage());
        return  ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        ErrorCode errorCode;
        Map<String, Objects> attributes = null;
        ApiResponse apiResponse = new ApiResponse();;
        try {
            errorCode  = ErrorCode.valueOf(exception.getFieldError().getDefaultMessage());
            apiResponse.setStatusCode(errorCode.getCode());
            var constraintViolation = exception.getBindingResult().getAllErrors().getFirst().unwrap(ConstraintViolation.class);
            attributes = constraintViolation.getConstraintDescriptor().getAttributes();
            apiResponse.setMessage(Objects.nonNull(attributes) ? mapAttribute(errorCode.getMessage(), attributes) : errorCode.getMessage());
        } catch (IllegalArgumentException e) {
            apiResponse.setStatusCode(ErrorCode.INVALID_KEY.getCode());
            apiResponse.setMessage(ErrorCode.INVALID_KEY.getMessage());
        }
        return  ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = AuthorizationDeniedException.class)
    ResponseEntity<ApiResponse> handlingAccessdeniedException(AuthorizationDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        return  ResponseEntity.status(errorCode.getStatusCode()).body(ApiResponse.builder()
                .statusCode(errorCode.getCode())
                .message(errorCode.getMessage())
                .build());
    }

    @ExceptionHandler(value = AuthorizationServiceException.class)
    ResponseEntity<ApiResponse> handlingAuthorizationException(AuthorizationServiceException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;
        return  ResponseEntity.status(errorCode.getCode()).body(ApiResponse.builder()
                .statusCode(errorCode.getCode())
                .message(errorCode.getMessage())
                .build());
    }

    private String mapAttribute(String message, Map<String, Objects> attributes) {
        String min = String.valueOf(attributes.get(MIN_ATTRIBUTE));
        return message.replace("{" + MIN_ATTRIBUTE + "}", min);
    }
}
