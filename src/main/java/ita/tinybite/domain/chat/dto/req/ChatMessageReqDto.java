package ita.tinybite.domain.chat.dto.req;

import ita.tinybite.domain.chat.entity.ChatMessage;
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

    public static ChatMessage of(ChatMessageReqDto req, Long userId) {
        String content = switch (req.messageType) {
            case TEXT -> req.text;
            case IMAGE -> req.imageUrl;
            case SYSTEM -> req.systemMessage;
            default -> null;
        };

        return ChatMessage.builder()
                .messageType(req.messageType())
                .chatRoomId(req.chatRoomId())
                .senderId(userId)
                .senderName(req.nickname())
                .content(content)
                .build();
    }
}
