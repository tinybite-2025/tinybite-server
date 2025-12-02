package ita.tinybite.domain.event.dto.request;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;
import ita.tinybite.domain.event.enums.RepeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@Schema(description = "일정 생성 요청 DTO")
public record EventReqDto(

	@Schema(description = "일정명", example = "팀 회의")
	@NotBlank(message = "일정명은 빈칸일 수 없습니다.")
	String title,

	@Schema(description = "하루 종일 여부", example = "false")
	@NotNull(message = "하루 종일 여부는 필수 값입니다.")
	Boolean allDay,

	@Schema(description = "시작 날짜 (YYYY-MM-DD)", example = "2025-11-05")
	@NotNull(message = "시작 날짜는 필수 값입니다.")
	LocalDate startDate,

	@Schema(description = "종료 날짜 (YYYY-MM-DD)", example = "2025-11-05")
	@NotNull(message = "종료 날짜는 필수 값입니다.")
	LocalDate endDate,

	@Schema(description = "시작 요일", example = "WEDNESDAY")
	@NotNull(message = "시작 요일은 필수 값입니다.")
	DayOfWeek startDay,

	@Schema(description = "종료 요일", example = "WEDNESDAY")
	@NotNull(message = "종료 요일은 필수 값입니다.")
	DayOfWeek endDay,

	@Schema(description = "시작 시간", example = "10:00")
	LocalTime startTime,

	@Schema(description = "종료 시간", example = "11:00")
	LocalTime endTime,

	@Schema(description = "반복 타입", example = "WEEK")
	@NotNull(message = "반복 타입은 필수 값입니다.")
	RepeatType repeatType,

	@Schema(description = "반복 횟수 (반복 없음일 경우 null)", example = "3")
	Integer repeatCount,

	@Schema(description = "반복 종료 날짜 (YYYY-MM-DD)", example = "2025-12-31")
	LocalDate repeatEndDate

	// @Schema(description = "투두 목록")
	// List<TodoDto> todos;
) {
}
