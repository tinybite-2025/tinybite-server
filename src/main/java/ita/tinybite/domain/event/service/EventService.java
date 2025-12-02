package ita.tinybite.domain.event.service;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ita.tinybite.domain.event.converter.EventConverter;
import ita.tinybite.domain.event.dto.request.EventReqDto;
import ita.tinybite.domain.event.dto.response.EventDetailResDto;
import ita.tinybite.domain.event.dto.response.EventListResDto;
import ita.tinybite.domain.event.dto.response.EventResDto;
import ita.tinybite.domain.event.entity.Event;
import ita.tinybite.domain.event.repository.EventRepository;
import ita.tinybite.domain.event.validator.EventValidator;
import ita.tinybite.global.exception.EventException;
import ita.tinybite.global.exception.errorcode.EventErrorCode;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class EventService {

	private final EventRepository eventRepository;

	public EventResDto createEvent(EventReqDto request) {
		Event event = EventConverter.toEntity(request);
		EventValidator.validateEventDay(event);
		event.getRepeatType().validateCount(event.getRepeatCount());
		//유저와 연관관계
		Event savedEvent = eventRepository.save(event);
		return EventConverter.toResponse(savedEvent);
	}

	public EventResDto updateEvent(long eventId, @Valid EventReqDto request) {
		Event event = eventRepository.findById(eventId)
			.orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));
		// 수정 권한 검증
		event.update(request);
		return EventConverter.toResponse(event);
	}

	public void deleteEvent(long eventId) {
		Event event = eventRepository.findById(eventId)
			.orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));
		// 삭제 권한 검증
		eventRepository.delete(event);
	}

	public EventDetailResDto getEventDetail(long eventId) {
		Event event = eventRepository.findById(eventId)
			.orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));
		return EventConverter.toEventDetailResponse(event);
	}

	public Page<EventResDto> getEventsByMonth(int year, int month, Pageable pageable) {
		if (month < 1 || month > 12) {
			throw new EventException(EventErrorCode.INVALID_DATE_RANGE);
		}

		LocalDate startDate = LocalDate.of(year, month, 1);
		LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

		Page<Event> events = eventRepository.findAllByMonth(startDate, endDate, pageable);

		return events.map(EventConverter::toResponse);
	}

	public Page<EventListResDto> getEventsByDate(LocalDate date, Pageable pageable) {
		Page<Event> events = eventRepository.findAllByDate(date, pageable);
		return events.map(EventConverter::toEventListResponse);
	}
}
