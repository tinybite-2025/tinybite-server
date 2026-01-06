package ita.tinybite.domain.chat.service;

import ita.tinybite.domain.chat.dto.res.ChatMessageResDto;
import ita.tinybite.domain.chat.dto.res.ChatMessageSliceResDto;
import ita.tinybite.domain.chat.entity.ChatMessage;
import ita.tinybite.domain.chat.entity.ChatRoomMember;
import ita.tinybite.domain.chat.repository.ChatMessageRepository;
import ita.tinybite.domain.chat.repository.ChatRoomRepository;
import ita.tinybite.domain.notification.service.facade.NotificationFacade;
import ita.tinybite.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatSubscribeRegistry registry;
    private final NotificationFacade notificationFacade;

    public ChatMessage saveMessage(ChatMessage message) {
        return chatMessageRepository.save(message);
    }

    /**
     * 구독하지 않은 채팅방 사용자에게 fcm알림을 전송함
     */
    public void sendNotification(ChatMessage message, Long chatRoomId) {
        // 1. 채팅방의 모든 참여자 조회
        Set<ChatRoomMember> participants = new HashSet<>(chatRoomRepository.findById(chatRoomId).orElseThrow().getParticipants());

        // 2. 이 중 구독하고있는 참여자 조회
        Set<Long> subscriberIds = registry.getSubscribers(chatRoomId);

        // 3. 모든 참여자에서 구독중인 참여자 & 본인 제외 -> 비구독자
        Set<User> unsubscribers = participants.stream()
                .map(ChatRoomMember::getUser)
                .filter(user -> !subscriberIds.contains(user.getUserId()))
                .filter(user -> !user.getUserId().equals(message.getSenderId()))
                .collect(Collectors.toSet());

        // 4. 해당 비구독자 유저들에게 메시지가 왔다는 알림 전송
        unsubscribers.forEach(unsubscriber ->
                notificationFacade.notifyNewChatMessage(unsubscriber.getUserId(), chatRoomId, unsubscriber.getNickname(), message.getContent())
        );
    }

    public ChatMessageSliceResDto getChatMessage(Long roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Slice<ChatMessage> messages = chatMessageRepository.findByChatRoomId(roomId, pageable);

        List<ChatMessageResDto> list = messages
                .getContent()
                .stream()
                .map(ChatMessageResDto::of)
                .toList();

        return ChatMessageSliceResDto.builder()
                .messages(list)
                .hasNext(messages.hasNext())
                .build();
    }
}
