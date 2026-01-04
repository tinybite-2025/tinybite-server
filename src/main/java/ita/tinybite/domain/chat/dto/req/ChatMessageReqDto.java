package ita.tinybite.domain.chat.dto.req;

public record ChatMessageReqDto(
        Long chatRoomId,
        Long userId,
        String nickname,
        String content
) {

}
