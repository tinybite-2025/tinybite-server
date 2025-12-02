package ita.tinybite.domain.task.validator;

import ita.tinybite.domain.event.entity.Event;
import ita.tinybite.domain.event.repository.EventRepository;
import ita.tinybite.domain.task.dto.request.CreateTaskReqDto;
import ita.tinybite.domain.task.dto.request.UpdateTaskRequestDto;
import ita.tinybite.domain.task.enums.TaskType;
import ita.tinybite.domain.task.service.UserProvider;
import ita.tinybite.global.exception.BusinessException;
import ita.tinybite.global.exception.EventException;
import ita.tinybite.global.exception.TaskException;
import ita.tinybite.global.exception.errorcode.EventErrorCode;
import ita.tinybite.global.exception.errorcode.TaskErrorCode;
import org.springframework.stereotype.Component;

@Component
public class TaskValidator {

    private final EventRepository eventRepository;
    private final UserProvider userProvider;

    public TaskValidator(EventRepository eventRepository,
                         UserProvider userProvider) {
        this.eventRepository = eventRepository;
        this.userProvider = userProvider;
    }

    public void createValidate(CreateTaskReqDto req) {
        // 제목 비었을 시 예외 throw
        if (req.title() == null)
            throw new TaskException(TaskErrorCode.INVALID_FIELD);

        switch (req.taskType()) {
            case IN_EVENT -> {
                if(req.eventId() == null)
                    throw new TaskException(TaskErrorCode.INVALID_FIELD);

                Event event = eventRepository
                        .findById(req.eventId())
                        .orElseThrow(() -> new TaskException(EventErrorCode.EVENT_NOT_FOUND));

                // 본인 확인
                if (!userProvider.currentUser().equals(event.getUser()))
                    throw new BusinessException(EventErrorCode.EVENT_NOT_OWNER);
            }
            case SCHEDULED -> {
                if(req.repeatType() == null || req.endDate() == null)
                    throw new TaskException(TaskErrorCode.INVALID_FIELD);
            }
            case UNSCHEDULED -> {}
            default -> throw new TaskException(TaskErrorCode.INVALID_FIELD);
        }
    }

    public void updateValidate(UpdateTaskRequestDto req) {
        if (req.title() == null)
            throw new TaskException(TaskErrorCode.INVALID_FIELD);

        if (req.taskType() == null)
            throw new TaskException(TaskErrorCode.INVALID_FIELD);

        if (req.taskType() == TaskType.SCHEDULED) {
            if (req.repeatType() == null || req.startDate() == null || req.endDate() == null)
                throw new TaskException(TaskErrorCode.INVALID_FIELD);
        }
    }

    public void deleteValidate(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new TaskException(EventErrorCode.EVENT_NOT_FOUND));

        if(!userProvider.currentUser().equals(event.getUser()))
            throw new EventException(EventErrorCode.EVENT_NOT_OWNER);
    }

    public void getTasksValidate(Long eventId) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new TaskException(EventErrorCode.EVENT_NOT_FOUND));
    }

    public void getTasksValidate() {
        
    }
}
