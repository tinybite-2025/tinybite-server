package ita.tinybite.global.exception;

import ita.tinybite.global.exception.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class TaskException extends RuntimeException {

    private final ErrorCode errorCode;

    public TaskException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public static TaskException of(ErrorCode errorCode) {
        return new TaskException(errorCode);
    }
}
