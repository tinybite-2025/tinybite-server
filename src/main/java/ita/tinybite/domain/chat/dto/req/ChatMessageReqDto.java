package ita.tinybite.domain.chat.dto.req;

import lombok.Builder;

@Builder
public record ChatMessageReqDto(
        Long chatRoomId,
        Long userId,
        String nickname,
        String content
) {

}
