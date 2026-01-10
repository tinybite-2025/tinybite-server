package ita.tinybite.global.exception.errorcode;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum PartyErrorCode implements ErrorCode {
	PARTY_NOT_FOUND(HttpStatus.NOT_FOUND, "PARTY_NOT_FOUND", "파티를 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	PartyErrorCode(HttpStatus httpStatus, String code, String message) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.message = message;
	}
}

