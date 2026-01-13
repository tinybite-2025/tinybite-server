package ita.tinybite.domain.party.enums;

import lombok.AccessLevel;
import lombok.Getter;;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum ParticipantStatus {
    PENDING("승인 대기"),
    APPROVED("승인 완료"),
    REJECTED("승인 거절"),

    REQUESTED("승인 요청"),
    ENDED("파티 종료")

    ;
    private final String description;
}
