package ita.tinybite.global.exception.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements ErrorCode {

    INVALID_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "INVALID_PHONE_NUMBER", "유효하지 않은 번호입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    AuthErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
