package ita.tinybite.domain.chat.dto.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import ita.tinybite.domain.chat.entity.ChatMessage;
import ita.tinybite.domain.chat.enums.MessageType;
import ita.tinybite.global.exception.BusinessException;
import ita.tinybite.global.exception.errorcode.BusinessErrorCode;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

        String systemMessage,

        String date
) {

    public static ChatMessageResDto of(ChatMessage message) {
        return ChatMessageResDto.of(message, null);
    }

    public static ChatMessageResDto of(ChatMessage chatMessage, Long senderId) {
        ChatMessageResDtoBuilder messageBuilder = ChatMessageResDto.builder()
                .messageId(chatMessage.getId())
                .messageType(chatMessage.getMessageType())
                .createdAt(chatMessage.getCreatedAt());


        switch (chatMessage.getMessageType()) {
            case SYSTEM -> {
                return messageBuilder
                        .systemMessage(chatMessage.getSystemMessage())
                        .build();
            }
            case DATE -> {
                return messageBuilder
                        .date(chatMessage.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                        .build();
            }
            case IMAGE -> {
                return messageBuilder
                        .senderId(chatMessage.getSenderId())
                        .nickname(chatMessage.getSenderName())
                        .isMine(senderId != null && senderId.equals(chatMessage.getSenderId()))
                        .imageUrl(chatMessage.getImageUrl())
                        .build();

            }
            case TEXT -> {
                return messageBuilder
                        .senderId(chatMessage.getSenderId())
                        .nickname(chatMessage.getSenderName())
                        .isMine(senderId != null && senderId.equals(chatMessage.getSenderId()))
                        .text(chatMessage.getText())
                        .build();
            }
            default -> throw BusinessException.of(BusinessErrorCode.INVALID_MESSAGE_TYPE);
        }
    }
}
