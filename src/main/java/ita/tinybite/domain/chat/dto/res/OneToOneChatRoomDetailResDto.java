package ita.tinybite.domain.chat.dto.res;


import com.fasterxml.jackson.annotation.JsonInclude;
import ita.tinybite.domain.chat.entity.ChatRoom;
import ita.tinybite.domain.chat.enums.ParticipantType;
import ita.tinybite.domain.party.entity.Party;
import ita.tinybite.domain.party.entity.PartyParticipant;
import ita.tinybite.domain.party.enums.ParticipantStatus;
import ita.tinybite.domain.user.entity.User;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OneToOneChatRoomDetailResDto(
        Long chatRoomId,
        Long groupChatRoomId,
        ParticipantType participantType, // 호스트인지, 참여자인지 구분
        ParticipantStatus participantStatus, // 파티 승인 현황 (호스트, 참여자 입장 구분 O)
        String targetName,
        String targetProfileImage,
        String targetLocation,
        String partyTitle
) {

    public static OneToOneChatRoomDetailResDto of(Party party, PartyParticipant partyParticipant, User currentUser, ChatRoom groupChatRoom) {
        User host = partyParticipant.getParty().getHost();
        User participant = partyParticipant.getUser();

        ParticipantType type = currentUser.getUserId().equals(host.getUserId()) ? ParticipantType.HOST : ParticipantType.PARTICIPANT;
        User targetUser = currentUser.getUserId().equals(participant.getUserId()) ? host : participant; // 상대방의 유저 (호스트면 참여자 / 참여자면 호스트)
        ParticipantStatus participantStatus = partyParticipant.getStatus();

        OneToOneChatRoomDetailResDtoBuilder resDtoBuilder = OneToOneChatRoomDetailResDto.builder()
                .chatRoomId(partyParticipant.getOneToOneChatRoom().getId())
                .participantType(type)
                .participantStatus(ParticipantStatus.PENDING)
                .targetName(targetUser.getNickname())
                .partyTitle(party.getTitle());

        switch(participantStatus) {
            case PENDING -> {
                if(type.equals(ParticipantType.HOST)) { // type = HOST
                    resDtoBuilder
                            .participantStatus(ParticipantStatus.REQUESTED)
                            .targetProfileImage(targetUser.getProfileImage())
                            .targetLocation(targetUser.getLocation());
                } else { // type = PARTICIPANT
                    resDtoBuilder
                            .participantStatus(ParticipantStatus.PENDING);
                }
            }
            case APPROVED -> {
                resDtoBuilder
                        .participantStatus(ParticipantStatus.APPROVED);
                if(type.equals(ParticipantType.HOST)) {
                    resDtoBuilder.groupChatRoomId(groupChatRoom != null ? groupChatRoom.getId() : null);
                }
            }
            case REJECTED -> {
                resDtoBuilder
                        .participantStatus(ParticipantStatus.REJECTED);
            }
            default -> throw new IllegalStateException("Unexpected value: " + participantStatus);
        }
        return resDtoBuilder.build();
    }
}

