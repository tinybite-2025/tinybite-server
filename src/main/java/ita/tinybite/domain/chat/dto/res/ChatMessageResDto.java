package ita.tinybite.domain.chat.dto.res;

import ita.tinybite.domain.chat.entity.ChatMessage;
import lombok.Builder;

@Builder
public record ChatMessageResDto(
        Long userId,
        String nickname,
        String content
) {
    public static ChatMessageResDto of(ChatMessage chatMessage) {
        return ChatMessageResDto.builder()
                .userId(chatMessage.getSenderId())
                .nickname(chatMessage.getSenderName())
                .content(chatMessage.getContent())
                .build();
    }
}
