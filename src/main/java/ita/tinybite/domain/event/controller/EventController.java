package ita.tinybite.domain.event.controller;

import java.time.LocalDate;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ita.tinybite.domain.event.dto.request.EventReqDto;
import ita.tinybite.domain.event.dto.response.EventDetailResDto;
import ita.tinybite.domain.event.dto.response.EventListResDto;
import ita.tinybite.domain.event.dto.response.EventResDto;
import ita.tinybite.domain.event.service.EventService;
import ita.tinybite.global.response.APIResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Tag(name = "일정", description = "일정 관련 API")
public class EventController {

	private final EventService eventService;

	@PostMapping
	@Operation(summary = "일정 생성 API",
		description = """
    	새로운 일정을 생성합니다.
    	요일(startDay, endDay)은 아래와 같은 문자열 중 하나로 입력해야 합니다:
    	- MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    """)
	public APIResponse<EventResDto> createEvent(@Valid  @RequestBody EventReqDto request) {
		EventResDto response = eventService.createEvent(request);
		return APIResponse.success(response);
	}

	@PutMapping("/{eventId}")
	@Operation(
		summary = "일정 수정 API",
		description = """
    	기존 일정을 수정합니다.
    	요일(startDay, endDay)은 아래 문자열 중 하나로 입력해야 합니다:
    	- MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    """
	)
	public APIResponse<EventResDto> updateEvent(
		@PathVariable long eventId,
		@Valid @RequestBody EventReqDto request) {
		EventResDto response = eventService.updateEvent(eventId, request);
		return APIResponse.success(response);
	}

	@DeleteMapping("/{eventId}")
	@Operation(summary = "일정 삭제 API", description = "일정을 삭제합니다.")
	public APIResponse<Void> deleteEvent(
		@PathVariable long eventId) {
		eventService.deleteEvent(eventId);
		return APIResponse.success(null);
	}

	@GetMapping("/{eventId}")
	@Operation(summary = "일정 상세 조회 API", description = "일정 ID로 상세 일정을 조회합니다.")
	public APIResponse<EventDetailResDto> getEvent(@PathVariable long eventId) {
		EventDetailResDto response = eventService.getEventDetail(eventId);
		return APIResponse.success(response);
	}

	@GetMapping("/month/{year}/{month}")
	@Operation(summary = "월별 전체 일정 조회 API", description = "해당 달의 전체 일정을 조회합니다.")
	public APIResponse<Page<EventResDto>> getEventsByMonth(
		@PathVariable int year,
		@PathVariable int month,
		@ParameterObject Pageable pageable) {
		Page<EventResDto> response = eventService.getEventsByMonth(year, month, pageable);
		return APIResponse.success(response);
	}

	@GetMapping("/day/{date}")
	@Operation(summary = "날짜별 전체 일정 조회 API",
		description = """
    	해당 날짜의 전체 일정을 조회합니다.
    	date는 YYYY-MM-DD 형식 (예: 2025-10-31)으로 입력해야 합니다.
    """
	)
	public APIResponse<Page<EventListResDto>> getEventsByDate(
		@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
		@ParameterObject Pageable pageable) {
		Page<EventListResDto> response = eventService.getEventsByDate(date, pageable);
		return APIResponse.success(response);
	}
}