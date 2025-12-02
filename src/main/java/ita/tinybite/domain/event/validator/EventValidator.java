package ita.tinybite.domain.event.validator;

import ita.tinybite.domain.event.entity.Event;
import ita.tinybite.global.exception.EventException;
import ita.tinybite.global.exception.errorcode.EventErrorCode;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EventValidator {
	public static void validateEventDay(Event event) {

		if (event.getStartDate() != null && event.getEndDate() != null) {
			if (event.getEndDate().isBefore(event.getStartDate())) {
				throw new EventException(EventErrorCode.INVALID_DATE_RANGE);
			}
		}

		if (Boolean.TRUE.equals(event.getAllDay())) {
			clearTimesIfAllDay(event);
		} else {
			validateEventTime(event);
		}
	}

	public static void clearTimesIfAllDay(Event event) {
		if(Boolean.TRUE.equals(event.getAllDay())){
			event.clearTimeIfAllDay();
		}
	}

	private static void validateEventTime(Event event) {
		if (event.getStartTime() == null || event.getEndTime() == null) {
			throw new EventException(EventErrorCode.MISSING_TIME);
		}

		if (event.getStartDate() != null && event.getEndDate() != null
			&& event.getStartDate().isEqual(event.getEndDate())
			&& event.getEndTime().isBefore(event.getStartTime())) {
			throw new EventException(EventErrorCode.INVALID_TIME_RANGE);
		}
	}
}
