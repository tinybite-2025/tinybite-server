package ita.tinybite.global.exception.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BusinessErrorCode implements ErrorCode {
    TEST(HttpStatus.INTERNAL_SERVER_ERROR, "TEST_ERROR_CODE", "테스트 에러코드입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "멤버를 찾을 수 없습니다."),

    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "INVALID_FILE_TYPE", "지원하지 않는 파일 타입입니다."),

    INVALID_MESSAGE_TYPE(HttpStatus.BAD_REQUEST, "INVALID_MESSAGE_TYPE" ,"지원하지 않는 메시지 타입입니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    BusinessErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
