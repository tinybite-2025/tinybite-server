package ita.tinybite.global.exception.errorcode;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum EventErrorCode implements ErrorCode {
	INVALID_VALUE(HttpStatus.BAD_REQUEST, "INVALID_VALUE", "잘못된 상태값입니다."),
	EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "일정을 찾을 수 없습니다."),
	INVALID_REPEAT_COUNT(HttpStatus.BAD_REQUEST, "INVALID_REPEAT_COUNT", "반복 횟수가 허용 범위를 벗어났습니다."),
	INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "INVALID_DATE_RANGE", "종료 날짜는 시작 날짜보다 앞설 수 없습니다."),
	MISSING_TIME(HttpStatus.BAD_REQUEST, "MISSING_TIME", "시작 시간과 종료 시간은 모두 입력되어야 합니다."),
	INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "INVALID_TIME_RANGE", "종료 시간은 시작 시간보다 같거나 뒤여야 합니다."),
    EVENT_NOT_OWNER(HttpStatus.FORBIDDEN, "EVENT_NOT_OWNER", "본인의 이벤트가 아닙니다.")
    ;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	EventErrorCode(HttpStatus httpStatus, String code, String message) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.message = message;
	}
}
