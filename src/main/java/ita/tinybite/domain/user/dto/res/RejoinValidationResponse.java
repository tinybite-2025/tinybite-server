package ita.tinybite.domain.user.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class RejoinValidationResponse {
    private boolean canRejoin;
    private Long daysRemaining;
    private LocalDateTime canRejoinAt;
    private String message;
}
