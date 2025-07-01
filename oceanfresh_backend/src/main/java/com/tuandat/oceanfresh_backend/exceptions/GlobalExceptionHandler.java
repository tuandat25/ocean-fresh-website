package com.tuandat.oceanfresh_backend.exceptions;


import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.tuandat.oceanfresh_backend.responses.ResponseObject;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseObject> handleResourceNotFoundException(ResourceNotFoundException exception,
                                                                      WebRequest webRequest) {
        // Tài khoản không tồn tại
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResponseObject.builder()
                        .status(HttpStatus.UNAUTHORIZED)
                        .message("Tài khoản không tồn tại. Vui lòng kiểm tra lại thông tin đăng nhập.")
                        .data(null)
                        .build());
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ResponseObject> handleInvalidPasswordException(InvalidPasswordException exception,
                                                                        WebRequest webRequest) {
        // Mật khẩu không chính xác
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResponseObject.builder()
                        .status(HttpStatus.UNAUTHORIZED)
                        .message("Số điện thoại/email hoặc mật khẩu không đúng.")
                        .data(null)
                        .build());
    }

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ResponseObject> handleDataNotFoundException(DataNotFoundException exception,
                                                                     WebRequest webRequest) {
        // Xử lý các trường hợp khác nhau dựa vào message
        String message = exception.getMessage();
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        
        if (message.contains("Tài khoản đã bị khóa") || message.contains("bị khóa")) {
            // Tài khoản bị khóa
            message = "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.";
        } else if (message.contains("Tài khoản không tồn tại")) {
            // Tài khoản không tồn tại
            message = "Số điện thoại/email hoặc mật khẩu không đúng.";
        } else {
            // Các trường hợp khác
            status = HttpStatus.NOT_FOUND;
        }
        
        return ResponseEntity.status(status)
                .body(ResponseObject.builder()
                        .status(status)
                        .message(message)
                        .data(null)
                        .build());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ResponseObject> handleDuplicateResourceException(DuplicateResourceException exception,
                                                                       WebRequest webRequest) {
        // Xử lý trường hợp email trùng lặp
        String message = exception.getMessage();
        
        // Kiểm tra nếu là lỗi từ social login
        if (message.contains("Xác thực không thành công") || message.contains("Email đã có tài khoản")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.CONFLICT)
                            .message(message)
                            .data(null)
                            .build());
        }
        
        // Các trường hợp khác (đăng ký thông thường)
        if (message.contains("email") || message.contains("Email")) {
            message = "Email này đã được sử dụng. Vui lòng sử dụng email khác.";
        } else if (message.contains("phone") || message.contains("số điện thoại")) {
            message = "Số điện thoại này đã được sử dụng. Vui lòng sử dụng số điện thoại khác.";
        }
        
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ResponseObject.builder()
                        .status(HttpStatus.CONFLICT)
                        .message(message)
                        .data(null)
                        .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseObject> handleIllegalArgumentException(IllegalArgumentException exception,
                                                                    WebRequest webRequest) {
        // Dữ liệu đầu vào không hợp lệ
        String message = exception.getMessage();
        if (message.contains("Google") || message.contains("google")) {
            message = "Xác thực Google thất bại. Vui lòng thử lại.";
        } else {
            message = "Dữ liệu đầu vào không hợp lệ. " + message;
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseObject.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .message(message)
                        .data(null)
                        .build());
    }

    @ExceptionHandler(SocialLoginException.class)
    public ResponseEntity<ResponseObject> handleSocialLoginException(SocialLoginException exception,
                                                                   WebRequest webRequest) {
        // Đăng nhập mạng xã hội thất bại
        String message = "Xác thực Google thất bại. Vui lòng thử lại.";
        
        if (exception.getMessage().contains("Facebook")) {
            message = "Đăng nhập Facebook thất bại. Vui lòng thử lại.";
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResponseObject.builder()
                        .status(HttpStatus.UNAUTHORIZED)
                        .message(message)
                        .data(null)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseObject> handleGlobalException(Exception exception,
                                                              WebRequest webRequest) {
        // Log the exception here
        logger.error("Unhandled exception occurred: ", exception);
        
        String message = "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.";
        
        // Xử lý một số trường hợp đặc biệt
        if (exception.getMessage() != null) {
            if (exception.getMessage().contains("Google") || exception.getMessage().contains("OAuth")) {
                message = "Xác thực Google thất bại. Vui lòng thử lại.";
            } else if (exception.getMessage().contains("Facebook")) {
                message = "Đăng nhập Facebook thất bại. Vui lòng thử lại.";
            }
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseObject.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .message(message)
                        .data(null)
                        .build());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        StringBuilder errorMessage = new StringBuilder("Dữ liệu đầu vào không hợp lệ. ");
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
            errorMessage.append(fieldName).append(": ").append(message).append(". ");
        });
        
        ResponseObject responseObject = ResponseObject.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message(errorMessage.toString().trim())
                .data(errors)
                .build();
                
        return new ResponseEntity<>(responseObject, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ResponseObject> handleInvalidInputException(InvalidInputException exception,
                                                                    WebRequest webRequest) {
        // Xử lý các lỗi validation đầu vào
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseObject.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .message(exception.getMessage())
                        .data(null)
                        .build());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseObject> handleDataIntegrityViolationException(DataIntegrityViolationException exception,
                                                                               WebRequest webRequest) {
        String message = "Dữ liệu không hợp lệ.";
        
        // Kiểm tra nếu là lỗi duplicate key cho email
        if (exception.getMessage() != null && exception.getMessage().contains("Duplicate entry") && 
            exception.getMessage().contains("for key 'email'")) {
            message = "Xác thực không thành công. Email đã có tài khoản đăng ký.";
        }
        // Kiểm tra nếu là lỗi duplicate key cho phone
        else if (exception.getMessage() != null && exception.getMessage().contains("Duplicate entry") && 
                 exception.getMessage().contains("for key 'phone")) {
            message = "Số điện thoại này đã được sử dụng. Vui lòng sử dụng số điện thoại khác.";
        }
        
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ResponseObject.builder()
                        .status(HttpStatus.CONFLICT)
                        .message(message)
                        .data(null)
                        .build());
    }
}
