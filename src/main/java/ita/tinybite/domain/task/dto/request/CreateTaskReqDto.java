package ita.tinybite.domain.task.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import ita.tinybite.domain.event.entity.Event;
import ita.tinybite.domain.event.enums.RepeatType;
import ita.tinybite.domain.task.entity.Task;
import ita.tinybite.domain.task.enums.TaskType;

import java.time.LocalDate;

@Schema(description = "할일 생성 요청 DTO")
public record CreateTaskReqDto(

        @Schema(description = "할 일 제목", example = "강아지 산책")
        String title,

        @Schema(description = "할 일 타입", example = "IN_EVENT")
        TaskType taskType,

        @Schema(description = "반복 단위", example = "NONE, DAY, WEEK ...")
        RepeatType repeatType,

        @Schema(description = "반복이 시작되는 날짜 지정", example = "2025-11-01")
        LocalDate startDate,

        @Schema(description = "반복이 끝나는 날짜 지정", example = "2025-11-08")
        LocalDate endDate,

        @Schema(description = "할 일이 일정에 포함될 때의 일정ID", example = "1")
        Long eventId
) {

    public Task convertAsInEvent(Event event) {
        return Task.builder()
                .title(title)
                .taskType(TaskType.IN_EVENT)
                .event(event)
                .build();
    }

    public Task convertAsScheduled() {
        return Task.builder()
                .title(title)
                .taskType(TaskType.SCHEDULED)
                .repeatType(repeatType)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    public Task convertAsUnscheduled() {
        return Task.builder()
                .title(title)
                .taskType(TaskType.UNSCHEDULED)
                .build();
    }
}
