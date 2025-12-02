package ita.tinybite.domain.event.entity;

import ita.tinybite.domain.event.dto.request.EventReqDto;
import ita.tinybite.domain.event.enums.RepeatType;
import ita.tinybite.domain.task.entity.Task;
import ita.tinybite.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Column(name = "event_title", nullable = false)
    private String title;

    @Column(nullable = false)
    private Boolean allDay;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    // 요일
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek startDay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek endDay;

    private LocalTime startTime;
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepeatType repeatType;

    private Integer repeatCount;

    private LocalDate repeatEndDate;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Task> tasks = new ArrayList<>();

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    public void clearTimeIfAllDay() {
        if (Boolean.TRUE.equals(allDay)) {
            this.startTime = null;
            this.endTime = null;
        }
    }

    public void update(EventReqDto request) {
        this.title = request.title();
        this.allDay = request.allDay();
        this.startDate = request.startDate();
        this.endDate = request.endDate();
        this.startDay = request.startDay();
        this.endDay = request.endDay();
        this.startTime = request.startTime();
        this.endTime = request.endTime();
        this.repeatType = request.repeatType();
        this.repeatCount = request.repeatCount();
        this.repeatEndDate = request.repeatEndDate();

        clearTimeIfAllDay();
    }
}
