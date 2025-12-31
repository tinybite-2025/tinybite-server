package ita.tinybite.domain.party.dto.response;

import ita.tinybite.domain.chat.enums.ChatRoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;

@AllArgsConstructor
@Builder
public class ChatRoomResponse {
    private Long id;
    private ChatRoomType type;
    private PartyInfo party;
}
