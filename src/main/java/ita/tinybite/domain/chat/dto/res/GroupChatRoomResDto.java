package ita.tinybite.domain.chat.dto.res;

import ita.tinybite.domain.chat.entity.ChatRoom;
import ita.tinybite.domain.chat.enums.ChatRoomType;
import ita.tinybite.domain.party.enums.PartyStatus;
import lombok.Builder;

@Builder
public record GroupChatRoomResDto(
        Long chatRoomId,
        ChatRoomType roomType,
        String partyTitle,
        String partyImage,
        String recentTime,
        Integer currentParticipantCnt,
        PartyStatus partyStatus,
        String recentMessage,
        Long unreadMessageCnt
) {

    public static GroupChatRoomResDto of(ChatRoom chatRoom, String timeAgo, String recentMessage, Long unreadCnt) {
        return GroupChatRoomResDto.builder()
                .chatRoomId(chatRoom.getId())
                .roomType(chatRoom.getType())
                .partyTitle(chatRoom.getParty().getTitle())
                .partyImage(chatRoom.getParty().getThumbnailImage())
                .recentTime(timeAgo)
                .currentParticipantCnt(chatRoom.getParty().getCurrentParticipants())
                .partyStatus(chatRoom.getParty().getStatus())
                .recentMessage(recentMessage)
                .unreadMessageCnt(unreadCnt)
                .build();
    }
}
