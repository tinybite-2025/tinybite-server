package ita.tinybite.global.exception.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum TaskErrorCode implements ErrorCode {
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "TASK_NOT_FOUND", "할일을 찾을 수 없습니다."),
    TASK_NOT_OWNER(HttpStatus.FORBIDDEN, "TASK_NOT_OWNER", "본인의 할일이 아닙니다."),
    INVALID_FIELD(HttpStatus.BAD_REQUEST, "INVALID_FIELD", "유효하지 않은 필드입니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    TaskErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
