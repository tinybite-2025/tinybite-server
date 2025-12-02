package ita.tinybite.domain.task.dto.response;

import lombok.Getter;

@Getter
public class TaskResponseDto {

    private String title;

    public TaskResponseDto(String title) {
        this.title = title;
    }
}
