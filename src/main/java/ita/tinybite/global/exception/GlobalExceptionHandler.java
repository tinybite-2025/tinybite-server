package ita.tinybite.global.exception;

import ita.tinybite.global.exception.errorcode.CommonErrorCode;
import ita.tinybite.global.exception.errorcode.ErrorCode;
import ita.tinybite.global.response.APIResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 이벤트 에러 처리
    @ExceptionHandler(EventException.class)
    public ResponseEntity<APIResponse<Void>> handleEventException(EventException exception) {
        log.error(exception.getMessage());

        return ResponseEntity.status(exception.getErrorCode().getHttpStatus())
            .body(APIResponse.businessError(exception.getErrorCode()));
    }

    // 비즈니스 에러 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<APIResponse<Void>> handleBusinessException(BusinessException exception) {
        log.error(exception.getMessage());

        return ResponseEntity.status(exception.getErrorCode().getHttpStatus())
                .body(APIResponse.businessError(exception.getErrorCode()));
    }

    // NOT_FOUND 에러 처리
    @ExceptionHandler({NoSuchElementException.class, NoResourceFoundException.class})
    public ResponseEntity<APIResponse<Void>> handle404Exception(
            Exception exception, HttpServletRequest request) {
        if (request.getRequestURI().endsWith(".ico")) return null; // 불필요한 리소스 요청은 무시

        ErrorCode errorCode = CommonErrorCode.NOT_FOUND;

        log.warn(errorCode.getMessage(), exception);
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(APIResponse.commonError(errorCode));
    }

    // BAD_REQUEST 에러 처리
    @ExceptionHandler({
        IllegalArgumentException.class,
        IllegalStateException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<APIResponse<Void>> handle400Exception(RuntimeException exception) {
        log.warn(exception.getMessage(), exception);

        ErrorCode errorCode = CommonErrorCode.BAD_REQUEST;
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(APIResponse.commonError(errorCode));
    }

    // 공통 에러 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<Void>> handle500Exception(Exception exception) {
        log.warn(exception.getMessage(), exception);

        ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(APIResponse.commonError(errorCode));
    }
}
