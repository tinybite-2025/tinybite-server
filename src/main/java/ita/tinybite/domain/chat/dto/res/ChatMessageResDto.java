package ita.tinybite.domain.chat.dto.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import ita.tinybite.domain.chat.entity.ChatMessage;
import ita.tinybite.domain.chat.enums.MessageType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    public static ChatMessageResDto of(ChatMessage message) {
        return ChatMessageResDto.of(message, null);
    }

    public static ChatMessageResDto of(ChatMessage chatMessage, Long senderId) {
        switch (chatMessage.getMessageType()) {
            case SYSTEM -> {
                return ChatMessageResDto.builder()
                        .messageId(chatMessage.getId())
                        .systemMessage(chatMessage.getSystemMessage())
                        .build();
            }
            case DATE -> {
                return ChatMessageResDto.builder()
                        .messageId(chatMessage.getId())
                        .createdAt(chatMessage.getCreatedAt())
                        .build();
            }
            case IMAGE -> {
                return ChatMessageResDto.builder()
                        .messageId(chatMessage.getId())
                        .senderId(chatMessage.getSenderId())
                        .nickname(chatMessage.getSenderName())
                        .messageType(chatMessage.getMessageType())
                        .createdAt(chatMessage.getCreatedAt())
                        .build();

            }
            case TEXT -> {
                return ChatMessageResDto.builder()
                        .messageId(chatMessage.getId())
                        .senderId(chatMessage.getSenderId())
                        .nickname(chatMessage.getSenderName())
                        .messageType(chatMessage.getMessageType())
                        .text(chatMessage.getText())
                        .createdAt(chatMessage.getCreatedAt())
                        .build();
            }
            default -> {

            }
        }
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
