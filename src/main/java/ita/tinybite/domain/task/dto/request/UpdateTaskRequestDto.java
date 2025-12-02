package ita.tinybite.domain.task.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import ita.tinybite.domain.event.enums.RepeatType;
import ita.tinybite.domain.task.enums.TaskType;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record UpdateTaskRequestDto(

        @Schema(name = "할 일 제목", description = "강아지 산책")
        String title,

        @Schema(description = "할 일 타입", example = "IN_EVENT")
        @NotBlank(message = "일정 내의 할일인지(IN_EVENT), 기간 지정인지(SCHEDULED), 기간 미지정인지(UNSCHEDULED) 입력해주세요")
        TaskType taskType,

        @Schema(description = "반복 단위", example = "NONE, DAY, WEEK ...")
        @Nullable
        RepeatType repeatType,

        @Schema(description = "반복이 시작되는 날짜 지정", example = "2025-11-01")
        @Nullable
        LocalDate startDate,

        @Schema(description = "반복이 끝나는 날짜 지정", example = "2025-11-08")
        @Nullable
        LocalDate endDate
) {
}
