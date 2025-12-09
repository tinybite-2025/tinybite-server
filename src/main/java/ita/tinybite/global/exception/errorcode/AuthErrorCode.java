package ita.tinybite.global.exception.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements ErrorCode {

    INVALID_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "INVALID_PHONE_NUMBER", "유효하지 않은 번호입니다."),
    INVALID_AUTHCODE(HttpStatus.UNAUTHORIZED, "INVALID_AUTHCODE", "인증코드가 일치하지 않습니다."),
    EXPIRED_AUTH_CODE(HttpStatus.BAD_REQUEST, "EXPIRED_AUTH_CODE", "인증시간이 만료되었습니다."),

    DUPLICATED_NICKNAME(HttpStatus.BAD_REQUEST, "DUPLICATED_NICKNAME", "중복된 닉네임입니다."),

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
