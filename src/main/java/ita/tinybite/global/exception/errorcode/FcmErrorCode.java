package ita.tinybite.global.exception.errorcode;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum FcmErrorCode implements ErrorCode{
	CANNOT_SEND_NOTIFICATION(HttpStatus.INTERNAL_SERVER_ERROR, "CANNOT_SEND_NOTIFICATION", "알림 메시지 전송에 실패했습니다."),
	FCM_TOKEN_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST,"FCM_TOKEN_LIMIT_EXCEEDED", "FCM 멀티캐스트 요청의 토큰 개수 제한을 초과했습니다.")
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	FcmErrorCode(HttpStatus httpStatus, String code, String message) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.message = message;
	}
}
