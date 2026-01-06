package ita.tinybite.global.exception;

import ita.tinybite.global.exception.errorcode.CommonErrorCode;
import ita.tinybite.global.exception.errorcode.ErrorCode;
import ita.tinybite.global.response.APIResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@ControllerAdvice
public class WsExceptionHandler {

    @MessageExceptionHandler(BusinessException.class)
    @SendToUser("/errors")
    public APIResponse<?> handleBusiness(BusinessException ex) {
        log.error("WebSocket BusinessException: {}", ex.getMessage(), ex);
        return APIResponse.businessError(ex.getErrorCode());
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser("/errors")
    public APIResponse<?> handle(Exception e) {
        log.error("WebSocket Exception: {}", e.getMessage(), e);
        ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
        return new APIResponse<>(errorCode.getHttpStatus().value(), errorCode.getHttpStatus().name(), e.getMessage(), LocalDateTime.now(), null);
    }
}
