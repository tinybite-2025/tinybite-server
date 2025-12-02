package ita.tinybite.domain.event.converter;

import ita.tinybite.domain.event.dto.request.EventReqDto;
import ita.tinybite.domain.event.dto.response.EventDetailResDto;
import ita.tinybite.domain.event.dto.response.EventListResDto;
import ita.tinybite.domain.event.dto.response.EventResDto;
import ita.tinybite.domain.event.entity.Event;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EventConverter {

	public Event toEntity(EventReqDto request) {
		return Event.builder()
			.title(request.title())
			.allDay(request.allDay())
			.startDate(request.startDate())
			.endDate(request.endDate())
			.startDay(request.startDay())
			.endDay(request.endDay())
			.startTime(request.startTime())
			.endTime(request.endTime())
			.repeatType(request.repeatType())
			.repeatCount(request.repeatCount())
			.repeatEndDate(request.repeatEndDate())
			.build();
	}

	public EventDetailResDto toEventDetailResponse(Event event) {
		return EventDetailResDto.builder()
			.id(event.getId())
			.title(event.getTitle())
			.startDate(event.getStartDate())
			.endDate(event.getEndDate())
			.startDay(event.getStartDay())
			.endDay(event.getEndDay())
			.startTime(event.getStartTime())
			.endTime(event.getEndTime())
			.repeatType(event.getRepeatType())
			.repeatCount(event.getRepeatCount())
			.repeatEndDate(event.getRepeatEndDate())
			.build();
	}

	public static EventResDto toResponse(Event event) {
		return EventResDto.builder()
			.eventId(event.getId())
			.title(event.getTitle())
			.startDate(event.getStartDate())
			.endDate(event.getEndDate())
			.build();
	}

	public static EventListResDto toEventListResponse(Event event) {
		return EventListResDto.builder()
			.eventId(event.getId())
			.title(event.getTitle())
			.allDay(event.getAllDay())
			.startTime(event.getStartTime())
			.endTime(event.getEndTime())
			.build();
	}
}
