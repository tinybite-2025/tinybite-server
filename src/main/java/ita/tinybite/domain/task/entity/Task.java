package ita.tinybite.domain.task.entity;

import ita.tinybite.domain.event.entity.Event;
import ita.tinybite.domain.event.enums.RepeatType;
import ita.tinybite.domain.task.dto.response.TaskResponseDto;
import ita.tinybite.domain.task.dto.request.UpdateTaskRequestDto;
import ita.tinybite.domain.task.enums.TaskType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id", nullable = false)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column
    private TaskType taskType;

    @Column
    private RepeatType repeatType;

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @JoinColumn(name = "event_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Event event;

    public TaskResponseDto of() {
        return new TaskResponseDto(this.title);
    }

    public void update(UpdateTaskRequestDto req) {
        this.title = req.title();
    }

    public void updateAsInEvent(UpdateTaskRequestDto req) {
        this.title = req.title();
        this.taskType = TaskType.IN_EVENT;
    }

    public void updateAsScheduled(UpdateTaskRequestDto req) {
        this.title = req.title();
        this.taskType = TaskType.SCHEDULED;
        this.repeatType = req.repeatType();
        this.startDate = req.startDate();
        this.endDate = req.endDate();
    }

    public void updateAsUnscheduled(UpdateTaskRequestDto req) {
        this.title = req.title();
        this.taskType = TaskType.UNSCHEDULED;
    }
}
