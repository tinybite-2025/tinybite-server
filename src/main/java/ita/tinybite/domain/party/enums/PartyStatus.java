package ita.tinybite.domain.party.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum PartyStatus {

    RECRUITING("모집 중", "참가자를 모집하고 있는 상태"),


    COMPLETED("모집 완료", "정원이 찼거나 모집이 완료된 상태"),

    CLOSED("종료됨", "파티가 정상적으로 종료된 상태"),

    CANCELLED("취소됨", "파티가 취소된 상태");

    private final String displayName;
    private final String description;
}
