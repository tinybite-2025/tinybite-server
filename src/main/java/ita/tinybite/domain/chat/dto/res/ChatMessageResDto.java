package ita.tinybite.domain.chat.dto.res;

import ita.tinybite.domain.chat.entity.ChatMessage;
import ita.tinybite.domain.chat.enums.MessageType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatMessageResDto(
        Long messageId,
        MessageType messageType,
        LocalDateTime createdAt,

        Long senderId,
        // 본인이 보낸 메시지인지 체크
        Boolean isMine,
        String nickname,
        String text,
        String imageUrl,

        String systemMessage
) {
    public static ChatMessageResDto of(ChatMessage chatMessage, Long senderId) {
        return ChatMessageResDto.builder()
                .messageId(chatMessage.getId())
                .messageType(chatMessage.getMessageType())
                .createdAt(chatMessage.getCreatedAt())
                .senderId(chatMessage.getSenderId())
                .isMine(senderId.equals(chatMessage.getSenderId()))
                .nickname(chatMessage.getSenderName())
                .text(chatMessage.getText())
                .imageUrl(chatMessage.getImageUrl())
                .systemMessage(chatMessage.getSystemMessage())
                .build();
    }
}
