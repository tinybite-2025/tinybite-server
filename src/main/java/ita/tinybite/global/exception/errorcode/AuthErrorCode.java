package ita.tinybite.global.exception.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements ErrorCode {

    INVALID_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "INVALID_PHONE_NUMBER", "유효하지 않은 번호입니다."),
    INVALID_AUTHCODE(HttpStatus.UNAUTHORIZED, "INVALID_AUTHCODE", "인증코드가 일치하지 않습니다."),
    EXPIRED_AUTH_CODE(HttpStatus.BAD_REQUEST, "EXPIRED_AUTH_CODE", "인증시간이 만료되었습니다."),

    DUPLICATED_NICKNAME(HttpStatus.BAD_REQUEST, "DUPLICATED_NICKNAME", "중복된 닉네임입니다."),

    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    GOOGLE_LOGIN_ERROR(HttpStatus.BAD_REQUEST, "GOOGLE_LOGIN_ERROR", "구글 로그인 중 에러가 발생했습니다."),
    APPLE_LOGIN_ERROR(HttpStatus.BAD_REQUEST, "APPLE_LOGIN_ERROR", "애플 로그인 중 에러가 발생했습니다."),

    INVALID_PLATFORM(HttpStatus.BAD_REQUEST, "INVALID_PLATFORM", "올바른 플랫폼이 아닙니다. (Android, iOS)"),
    NOT_EXISTS_EMAIL(HttpStatus.BAD_REQUEST, "NOT_EXISTS_EMAIL", "애플 이메일이 존재하지 않습니다."),
    INVALID_LOCATION(HttpStatus.BAD_REQUEST, "INVALID_LOCATION", "위치 정보가 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    AuthErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
