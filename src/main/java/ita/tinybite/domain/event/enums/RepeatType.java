package ita.tinybite.domain.event.enums;

import ita.tinybite.global.exception.EventException;
import ita.tinybite.global.exception.errorcode.EventErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum RepeatType {
	NONE("반복 안 함", 0, 0),
	DAY("매일", 1, 6),
	WEEK("주 단위", 1, 5),
	MONTH("월 단위", 1, 5),
	YEAR("년 단위", 1, 3);

	private final String description;
	private final int minCount;
	private final int maxCount;

	public static RepeatType from(String value) {
		for (RepeatType repeatType : values()) {
			if (repeatType.name().equals(value)) {
				return repeatType;
			}
		}
		throw new EventException(EventErrorCode.INVALID_VALUE);
	}

	public void validateCount(Integer count) {
		if (this == NONE) {
			if (count != null && count != 0) {
				throw new EventException(EventErrorCode.INVALID_REPEAT_COUNT);
			}
			return;
		}

		if (count == null || count < minCount || count > maxCount) {
			throw new EventException(EventErrorCode.INVALID_REPEAT_COUNT);
		}
	}
}
