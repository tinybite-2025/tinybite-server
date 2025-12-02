package ita.tinybite.domain.task.service;

import ita.tinybite.domain.event.entity.Event;
import ita.tinybite.domain.event.repository.EventRepository;
import ita.tinybite.domain.task.dto.request.CreateTaskReqDto;
import ita.tinybite.domain.task.dto.response.TaskResponseDto;
import ita.tinybite.domain.task.dto.request.UpdateTaskRequestDto;
import ita.tinybite.domain.task.entity.Task;
import ita.tinybite.domain.task.repository.TaskRepository;
import ita.tinybite.domain.task.validator.TaskValidator;
import ita.tinybite.global.exception.BusinessException;
import ita.tinybite.global.exception.TaskException;
import ita.tinybite.global.exception.errorcode.EventErrorCode;
import ita.tinybite.global.exception.errorcode.TaskErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@AllArgsConstructor
public class TaskService {

    private final EventRepository eventRepository;
    private final TaskRepository taskRepository;
    private final TaskValidator taskValidator;
    private final UserProvider userProvider;

    @Transactional
    public void createTask(CreateTaskReqDto req) {
        taskValidator.createValidate(req);

        switch (req.taskType()) {
            case IN_EVENT -> saveAsInEvent(req);
            case SCHEDULED -> saveAsScheduled(req);
            case UNSCHEDULED -> saveAsUnscheduled(req);
        }
    }

    @Transactional
    public void updateTask(Long taskId, UpdateTaskRequestDto req) {
        taskValidator.updateValidate(req);

        Task task = taskRepository
                        .findById(taskId)
                        .orElseThrow(() -> new TaskException(TaskErrorCode.TASK_NOT_FOUND));

        switch (req.taskType()) {
            case IN_EVENT -> task.updateAsInEvent(req);
            case SCHEDULED -> task.updateAsScheduled(req);
            case UNSCHEDULED -> task.updateAsUnscheduled(req);
        }
    }

    @Transactional
    public void deleteTask(Long eventId, Long taskId) {
        taskValidator.deleteValidate(eventId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskException(TaskErrorCode.TASK_NOT_FOUND));

        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public Slice<TaskResponseDto> getTasks(Long eventId, Pageable pageable) {
        taskValidator.getTasksValidate(eventId);

        return taskRepository.findByEventId(eventId, pageable)
                .map(Task::of);
    }

    @Transactional(readOnly = true)
    public Slice<TaskResponseDto> getTodayTasks(Pageable pageable) {
        return taskRepository.findTodayTasksByUser(userProvider.currentUser(), pageable)
                .map(Task::of);
    }

    @Transactional(readOnly = true)
    public Slice<TaskResponseDto> getSomedayTasks(Pageable pageable) {
        return taskRepository.findSomedayTasksByUser(userProvider.currentUser(), pageable)
                .map(Task::of);
    }


    // 일정에 포함된 할 일 저장 로직
    private void saveAsInEvent(CreateTaskReqDto req) {
        assert req.eventId() != null;
        Event event = eventRepository
                .findById(req.eventId())
                .orElseThrow(() -> new BusinessException(EventErrorCode.EVENT_NOT_FOUND));
        taskRepository.save(req.convertAsInEvent(event));
    }

    // 기간 지정 할 일 저장 로직
    private void saveAsScheduled(CreateTaskReqDto req) {
        taskRepository.save(req.convertAsScheduled());
    }

    // 기간 미지정 할 일 저장 로직
    private void saveAsUnscheduled(CreateTaskReqDto req) {
        taskRepository.save(req.convertAsUnscheduled());
    }
}
