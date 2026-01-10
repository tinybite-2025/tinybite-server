package ita.tinybite.domain.chat.dto.res;

import ita.tinybite.domain.chat.entity.ChatRoom;
import ita.tinybite.domain.chat.enums.ChatRoomType;
import ita.tinybite.domain.party.enums.ParticipantStatus;
import ita.tinybite.domain.user.entity.User;
import lombok.Builder;

@Builder
public record OneToOneChatRoomResDto(
    Long chatRoomId,
    ChatRoomType roomType,
    Long myId,
    String myProfileImage,
    Long targetId,
    String targetProfileImage,
    String recentTime,
    String partyTitle,
    ParticipantStatus status,
    String recentMessage,
    Long unreadMessageCnt
) {

    public static OneToOneChatRoomResDto of(ChatRoom chatRoom, User myUser, User targetUser, String messageContent, String timeAgo, ParticipantStatus status, long unreadCnt) {
        return OneToOneChatRoomResDto.builder()
                .chatRoomId(chatRoom.getId())
                .roomType(chatRoom.getType())
                .myId(myUser.getUserId())
                .myProfileImage(myUser.getProfileImage())
                .targetId(targetUser.getUserId())
                .targetProfileImage(targetUser.getProfileImage())
                .recentTime(timeAgo)
                .partyTitle(chatRoom.getParty().getTitle())
                .status(status)
                .recentMessage(messageContent)
                .unreadMessageCnt(unreadCnt)
                .build();
    }
}
