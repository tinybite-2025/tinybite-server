package ita.tinybite.domain.chat.service;

import ita.tinybite.domain.auth.service.SecurityProvider;
import ita.tinybite.domain.chat.dto.GroupChatRoomDetailResDto;
import ita.tinybite.domain.chat.dto.res.GroupChatRoomResDto;
import ita.tinybite.domain.chat.dto.res.OneToOneChatRoomDetailResDto;
import ita.tinybite.domain.chat.dto.res.OneToOneChatRoomResDto;
import ita.tinybite.domain.chat.entity.ChatMessage;
import ita.tinybite.domain.chat.entity.ChatRoom;
import ita.tinybite.domain.chat.entity.ChatRoomMember;
import ita.tinybite.domain.chat.enums.ChatRoomType;
import ita.tinybite.domain.chat.enums.ParticipantType;
import ita.tinybite.domain.chat.repository.ChatMessageRepository;
import ita.tinybite.domain.chat.repository.ChatRoomMemberRepository;
import ita.tinybite.domain.chat.repository.ChatRoomRepository;
import ita.tinybite.domain.party.entity.PartyParticipant;
import ita.tinybite.domain.party.enums.ParticipantStatus;
import ita.tinybite.domain.party.repository.PartyParticipantRepository;
import ita.tinybite.domain.user.entity.User;
import ita.tinybite.global.exception.BusinessException;
import ita.tinybite.global.exception.errorcode.ChatRoomErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final SecurityProvider securityProvider;
    private final PartyParticipantRepository partyParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;


    public List<OneToOneChatRoomResDto> getOneToOneRooms() {
        User currentUser = securityProvider.getCurrentUser();

        List<ChatRoom> chatRooms = chatRoomMemberRepository.findByUserOrderByLatestMessage(currentUser, ChatRoomType.ONE_TO_ONE);

        return chatRooms.stream()
                .map(chatRoom -> {
                    // chatRoom으로 가장 최신의 메시지 조회
                    ChatMessage recentMessage = chatMessageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoom.getId(), Limit.of(1));

                    // 최신 메시지로부터 시간 계산 (최신 메시지가 없는 경우 "")
                    String timeAgo = (recentMessage != null && recentMessage.getCreatedAt() != null)
                            ? getTimeAgo(recentMessage.getCreatedAt())
                            : "";

                    // chatRoom을 통해 상태 조회
                    PartyParticipant partyParticipant = partyParticipantRepository.findByOneToOneChatRoom(chatRoom).orElseThrow();
                    ParticipantStatus status = partyParticipant.getStatus();

                    // 호스트 - 참여자 엔티티
                    User partyParticipantUser = partyParticipant.getUser();
                    User host = chatRoom.getParty().getHost();

                    ChatRoomMember chatRoomMember = chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, currentUser).orElseThrow();

                    // 마지막으로 읽은 시점을 기점으로 몇 개의 메시지가 안 읽혔는지 확인
                    long unreadCnt = chatMessageRepository.countByChatRoomIdAndCreatedAtAfterAndSenderIdNot(chatRoom.getId(), chatRoomMember.getLastReadAt(), currentUser.getUserId());

                    String content = recentMessage != null ? recentMessage.getContent() : "";

                    // 현재 유저가 참여자라면, targetUser는 호스트
                    User targetUser = currentUser.getUserId().equals(partyParticipantUser.getUserId()) ? host : partyParticipantUser;
                    return OneToOneChatRoomResDto.of(chatRoom, currentUser, targetUser, content, timeAgo, status, unreadCnt);
                })
                .toList();
    }


    public List<GroupChatRoomResDto> getGroupRooms() {
        User user = securityProvider.getCurrentUser();

        // 유저가 참여 중인 chatRoom (이면서 그룹 채팅만)
        List<ChatRoom> chatRooms = chatRoomMemberRepository.findByUserOrderByLatestMessage(user, ChatRoomType.GROUP).stream()
                .filter(chatRoom -> chatRoom.getType().equals(ChatRoomType.GROUP))
                .filter(chatRoom -> chatRoom.getParticipants().size() > 1)
                .toList();

        return chatRooms.stream()
                .map(chatRoom -> {
                    // chatRoom으로 가장 최신의 메시지 조회
                    ChatMessage recentMessage = chatMessageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoom.getId(), Limit.of(1));
                    String recentContent = recentMessage != null ? recentMessage.getContent() : null;
                    String timeAgo = getTimeAgo(chatRoom.getCreatedAt());

                    ChatRoomMember chatRoomMember = chatRoomMemberRepository.findByChatRoomAndUserAndChatRoom_Type(chatRoom, user, ChatRoomType.GROUP).stream().findFirst().get();

                    // 마지막으로 읽은 시점을 기점으로 몇 개의 메시지가 안 읽혔는지 확인
                    long unreadCnt = chatMessageRepository.countByChatRoomIdAndCreatedAtAfterAndSenderIdNot(chatRoom.getId(), chatRoomMember.getLastReadAt(), user.getUserId());

                    return GroupChatRoomResDto.of(chatRoom, timeAgo, recentContent, unreadCnt);
                })
                .toList();
    }

    public OneToOneChatRoomDetailResDto getOneToOneRoom(Long chatRoomId) {
        User currentUser = securityProvider.getCurrentUser();
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow();

        if(!chatRoom.getType().equals(ChatRoomType.ONE_TO_ONE)) throw BusinessException.of(ChatRoomErrorCode.NOT_ONE_TO_ONE);

        PartyParticipant partyParticipant = partyParticipantRepository.findByOneToOneChatRoom(chatRoom).orElseThrow();

        User host = partyParticipant.getParty().getHost();
        User participant = partyParticipant.getUser();

        // 현재 사용자의 ParticipantType 판단
        ParticipantType type = currentUser.getUserId().equals(host.getUserId()) ? ParticipantType.HOST : ParticipantType.PARTICIPANT;
        User targetUser = currentUser.getUserId().equals(participant.getUserId()) ? host : participant;

        ChatRoom groupChatRoom = chatRoomRepository.findByPartyAndType(chatRoom.getParty(), ChatRoomType.GROUP).orElseGet(null);
        return OneToOneChatRoomDetailResDto.of(chatRoom.getParty(), partyParticipant, groupChatRoom, targetUser, type);
    }

    public GroupChatRoomDetailResDto getGroupRoom(Long chatRoomId) {
        User currentUser = securityProvider.getCurrentUser();

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow();

        User host = chatRoom.getParty().getHost();

        if(!chatRoom.getType().equals(ChatRoomType.GROUP)) throw BusinessException.of(ChatRoomErrorCode.NOT_GROUP);

        ParticipantType participantType;
        if(currentUser.getUserId().equals(host.getUserId())) participantType = ParticipantType.HOST;
        else participantType = ParticipantType.PARTICIPANT;

        return GroupChatRoomDetailResDto.of(chatRoom, participantType);
    }

    private static String getTimeAgo(LocalDateTime then) {
        LocalDateTime now = LocalDateTime.now();

        // 전체 경과 시간을 분 단위로 계산
        long minutes = java.time.Duration.between(then, now).toMinutes();

        // 1분 미만
        if (minutes < 1) {
            return "방금 전";
        }

        // 1시간 미만 (1~59분)
        if (minutes < 60) {
            return minutes + "분 전";
        }

        // 24시간 미만 (1~23시간)
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + "시간 전";
        }

        // 30일 미만 (1~29일)
        long days = hours / 24;
        if (days < 30) {
            return days + "일 전";
        }

        // 12개월 미만 (1~11개월)
        long months = days / 30;
        if (months < 12) {
            return months + "개월 전";
        }

        // 1년 이상
        long years = months / 12;
        return years + "년 전";
    }
}
