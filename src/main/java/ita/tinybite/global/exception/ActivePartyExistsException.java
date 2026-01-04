package ita.tinybite.global.exception;

import ita.tinybite.global.exception.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class ActivePartyExistsException extends RuntimeException {
    public ActivePartyExistsException(String message) {
        super(message);
    }
}
