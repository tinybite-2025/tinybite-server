package ita.tinybite.domain.event.dto.response;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;
import ita.tinybite.domain.event.enums.RepeatType;
import lombok.Builder;

@Builder
@Schema(description = "일정 상세 조회 DTO")
public record EventDetailResDto(

	@Schema(description = "일정 ID", example = "1")
	Long id,

	@Schema(description = "일정명", example = "팀 회의")
	String title,

	@Schema(description = "하루 종일 여부", example = "false")
	Boolean allDay,

	@Schema(description = "시작 날짜 (YYYY-MM-DD)", example = "2025-11-05")
	LocalDate startDate,

	@Schema(description = "종료 날짜 (YYYY-MM-DD)", example = "2025-11-05")
	LocalDate endDate,

	@Schema(description = "시작 요일", example = "WEDNESDAY")
	DayOfWeek startDay,

	@Schema(description = "종료 요일", example = "WEDNESDAY")
	DayOfWeek endDay,

	@Schema(description = "시작 시간", example = "10:00")
	LocalTime startTime,

	@Schema(description = "종료 시간", example = "11:00")
	LocalTime endTime,

	@Schema(description = "반복 타입", example = "WEEK")
	RepeatType repeatType,

	@Schema(description = "반복 횟수 (반복 없음일 경우 null)", example = "3")
	Integer repeatCount,

	@Schema(description = "반복 종료 날짜 (YYYY-MM-DD)", example = "2025-12-31")
	LocalDate repeatEndDate

	// @Schema(description = "투두 목록")
	// List<TodoDto> todos;
) {
}
