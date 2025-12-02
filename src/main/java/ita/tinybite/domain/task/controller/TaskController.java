package ita.tinybite.domain.task.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ita.tinybite.domain.task.dto.request.CreateTaskReqDto;
import ita.tinybite.domain.task.dto.request.UpdateTaskRequestDto;
import ita.tinybite.domain.task.service.TaskService;
import ita.tinybite.global.response.APIResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "할 일", description = "할 일 관련 API")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @Operation(summary = "할 일 생성 API",
        description = """
        새로운 할 일을 생성합니다.
        
    """)
    public APIResponse<?> createTask(
            @RequestBody CreateTaskReqDto req) {
        taskService.createTask(req);
        return APIResponse.success();
    }

    @PatchMapping("/{taskId}")
    @Operation(summary = "할 일 수정 API",
        description = """
        할 일을 수정합니다.
        일정에 포함된 할 일을 생성할 때는, eventId QueryParam을 추가해주세요.
        일정에 포함되지 않는 할 일은 eventId가 필요하지 않습니다.
    """)
    public APIResponse<?> updateTask(
            @PathVariable Long taskId,
            @RequestBody UpdateTaskRequestDto req) {
        taskService.updateTask(taskId, req);
        return APIResponse.success();
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "할 일 삭제 API",
    description = """
        할 일을 삭제합니다.
        이 또한 eventId로 일정에 포함된 할 일인지 구분하게 됩니다.
    """)
    public APIResponse<?> deleteTask(
            @RequestParam(value = "eventId", required = false) Long eventId,
            @PathVariable Long taskId) {
        taskService.deleteTask(eventId, taskId);
        return APIResponse.success();
    }

    @GetMapping("/today")
    @Operation(summary = "오늘 할 일 조회",
    description = """
            오늘 할 일을 조회합니다. 10개씩 Slice로 데이터가 순서대로 조회됩니다 (생성순).
            """)
    public APIResponse<?> getTodayTasks(
            @PageableDefault(size = 20) Pageable pageable) {
        return APIResponse.success(taskService.getTodayTasks(pageable));
    }

    @GetMapping("/someday")
    @Operation(summary = "언젠가 할 일 조회",
            description = """
            언젠가 할 일을 조회합니다. 10개씩 Slice로 데이터가 순서대로 조회됩니다 (생성순).
            """)
    public APIResponse<?> getSomedayTasks(
            @PageableDefault(size = 20) Pageable pageable) {
        return APIResponse.success(taskService.getSomedayTasks(pageable));
    }
}
