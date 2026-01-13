package ita.tinybite.global.exception.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ChatRoomErrorCode implements ErrorCode {

    NOT_ONE_TO_ONE(HttpStatus.BAD_REQUEST, "NOT_ONE_TO_ONE", "일대일 채팅이 아닙니다."),
    NOT_GROUP(HttpStatus.BAD_REQUEST, "NOT_GROUP", "그룹 채팅이 아닙니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ChatRoomErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
