package ita.tinybite.domain.chat.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum ChatRoomType {
        ONE_TO_ONE("1:1 채팅"),
        GROUP("단체 채팅");

        private final String description;
}
