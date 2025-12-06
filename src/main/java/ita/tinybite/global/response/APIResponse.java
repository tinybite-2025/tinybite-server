package ita.tinybite.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import ita.tinybite.global.exception.errorcode.ErrorCode;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record APIResponse<T>(
        int status,
        String code, // 비즈니스 에러일 때만 값 존재
        String message,
        LocalDateTime timestamp,
        T data // 성공일 때만 값 존재
        ) {

    private static final String SUCCESS_MESSAGE = "요청이 성공적으로 처리되었습니다.";

    // 성공응답 (data X)
    public static <T> APIResponse<T> success() {
        return new APIResponse<>(
                HttpStatus.OK.value(), null, SUCCESS_MESSAGE, LocalDateTime.now(), null);
    }

    // 성공응답 (data 존재)
    public static <T> APIResponse<T> success(T data) {
        return new APIResponse<>(
                HttpStatus.OK.value(), null, SUCCESS_MESSAGE, LocalDateTime.now(), data);
    }

    // 실패응답 (공통)
    public static <T> APIResponse<T> commonError(ErrorCode errorCode) {
        return new APIResponse<>(
                errorCode.getHttpStatus().value(),
                null,
                errorCode.getMessage(),
                LocalDateTime.now(),
                null);
    }

    // 실패응답 (비즈니스)
    public static <T> APIResponse<T> businessError(ErrorCode errorCode) {
        return new APIResponse<>(
                errorCode.getHttpStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                LocalDateTime.now(),
                null);
    }
}
