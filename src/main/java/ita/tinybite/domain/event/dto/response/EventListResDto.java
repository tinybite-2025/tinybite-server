package ita.tinybite.domain.event.dto.response;

import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "일정 리스트 응답 DTO")
public record EventListResDto(
	@Schema(description = "생성된 일정 ID", example = "1")
	Long eventId,

	@Schema(description = "일정명", example = "팀 회의")
	String title,

	@Schema(description = "하루 종일 여부", example = "false")
	Boolean allDay,

	@Schema(description = "시작 시간", example = "10:00")
	LocalTime startTime,

	@Schema(description = "종료 시간", example = "11:00")
	LocalTime endTime

	// @Schema(description = "투두 목록")
	// List<TodoDto> todos;
) {
}
