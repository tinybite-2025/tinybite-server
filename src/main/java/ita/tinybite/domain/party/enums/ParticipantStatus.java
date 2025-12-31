package ita.tinybite.domain.party.enums;

import lombok.AccessLevel;
import lombok.Getter;;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum ParticipantStatus {
    PENDING("승인 대기"),
    APPROVED("승인됨"),
    REJECTED("거절됨");

    private final String description;
}
