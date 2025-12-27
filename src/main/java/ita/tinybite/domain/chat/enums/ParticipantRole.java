package ita.tinybite.domain.chat.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum ParticipantRole {
    LEADER("리더", "파티를 생성하고 관리하는 리더"),
    MEMBER("멤버", "일반 참가자");
    private final String displayName;
    private final String description;
}
