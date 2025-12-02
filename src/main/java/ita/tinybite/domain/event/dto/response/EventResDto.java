package ita.tinybite.domain.event.dto.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "일정 생성 응답 DTO")
public record EventResDto(
	@Schema(description = "생성된 일정 ID", example = "1")
	Long eventId,

	@Schema(description = "일정명", example = "팀 회의")
	String title,

	@Schema(description = "시작 날짜 (YYYY-MM-DD)", example = "2025-11-05")
		LocalDate startDate,

	@Schema(description = "종료 날짜 (YYYY-MM-DD)", example = "2025-11-05")
	LocalDate endDate
) {
}
