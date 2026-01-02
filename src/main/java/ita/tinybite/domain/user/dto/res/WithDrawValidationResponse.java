package ita.tinybite.domain.user.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class WithDrawValidationResponse {
    private boolean canWithdraw;
    private long activePartyCount;
    private long hostPartyCount;
    private long participantPartyCount;
    private String message;
}
