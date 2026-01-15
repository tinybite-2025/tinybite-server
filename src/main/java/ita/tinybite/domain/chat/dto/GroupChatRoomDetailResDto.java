package ita.tinybite.domain.chat.dto;

import ita.tinybite.domain.chat.entity.ChatRoom;
import ita.tinybite.domain.chat.enums.ChatRoomType;
import ita.tinybite.domain.party.enums.PartyStatus;
import lombok.Builder;

@Builder
public record GroupChatRoomDetailResDto(
        Long groupChatRoomId,
        ChatRoomType roomType,
        Long partyId,
        String partyTitle,
        PartyStatus status,
        int currentParticipantCnt,
        int maxParticipantCnt,
        String formattedPartyCnt
) {
    public static GroupChatRoomDetailResDto of(ChatRoom groupChatRoom) {
        return GroupChatRoomDetailResDto.builder()
                .groupChatRoomId(groupChatRoom.getId())
                .roomType(groupChatRoom.getType())
                .partyId(groupChatRoom.getParty().getId())
                .partyTitle(groupChatRoom.getParty().getTitle())
                .status(groupChatRoom.getParty().getStatus())
                .currentParticipantCnt(groupChatRoom.getParty().getCurrentParticipants())
                .maxParticipantCnt(groupChatRoom.getParty().getMaxParticipants())
                .formattedPartyCnt(formatParticipantStatus(groupChatRoom.getParty().getCurrentParticipants(), groupChatRoom.getParty().getMaxParticipants()))
                .build();
    }

    private static String formatParticipantStatus(int current, int max) {
        return String.format("%d/%d명", current, max);
    }

}
