package ita.tinybite.domain.chat.dto.req;

import ita.tinybite.domain.chat.enums.MessageType;
import lombok.Builder;

@Builder
public record ChatMessageReqDto(
        MessageType messageType,
        Long chatRoomId,
        String nickname,
        String text,
        String imageUrl,
        String systemMessage
) {

}
