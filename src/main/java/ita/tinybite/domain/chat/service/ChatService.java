package ita.tinybite.domain.chat.service;

import ita.tinybite.domain.auth.service.SecurityProvider;
import ita.tinybite.domain.chat.dto.res.ChatMessageResDto;
import ita.tinybite.domain.chat.dto.res.ChatMessageSliceResDto;
import ita.tinybite.domain.chat.entity.ChatMessage;
import ita.tinybite.domain.chat.entity.ChatRoom;
import ita.tinybite.domain.chat.entity.ChatRoomMember;
import ita.tinybite.domain.chat.enums.ChatRoomType;
import ita.tinybite.domain.chat.enums.MessageType;
import ita.tinybite.domain.chat.repository.ChatMessageRepository;
import ita.tinybite.domain.chat.repository.ChatRoomMemberRepository;
import ita.tinybite.domain.chat.repository.ChatRoomRepository;
import ita.tinybite.domain.notification.service.facade.NotificationFacade;
import ita.tinybite.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
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

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatSubscribeRegistry registry;
    private final NotificationFacade notificationFacade;
    private final SecurityProvider securityProvider;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

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
        String messageContent;
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow();

        // 1대1일 때
        if (chatRoom.getType().equals(ChatRoomType.ONE_TO_ONE)) {
            switch (message.getMessageType()) {
                case TEXT -> {
                    messageContent = message.getContent();
                    unsubscribers.forEach(unsubscriber ->
                            notificationFacade.notifyOneToOneChat(unsubscriber.getUserId(), chatRoomId, unsubscriber.getNickname(), messageContent)
                    );
                }
                case IMAGE -> {
                    unsubscribers.forEach(unsubscriber ->
                            notificationFacade.notifyOneToOneImage(unsubscriber.getUserId(), chatRoomId, unsubscriber.getNickname())
                    );
                }
            }
        } else { // 그룹일 때
            switch (message.getMessageType()) {
                case TEXT -> {
                    messageContent = message.getContent();
                    unsubscribers.forEach(unsubscriber ->
                            notificationFacade.notifyGroupChat(unsubscriber.getUserId(), chatRoomId, chatRoom.getParty().getTitle(), message.getSenderName(), messageContent)
                    );
                }
                case IMAGE -> {
                    unsubscribers.forEach(unsubscriber ->
                            notificationFacade.notifyGroupImage(unsubscriber.getUserId(), chatRoomId, chatRoom.getParty().getTitle(), message.getSenderName())
                    );
                }
            }
        }
    }

    public ChatMessageSliceResDto getChatMessage(Long roomId, int page, int size) {
        User user = securityProvider.getCurrentUser();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Slice<ChatMessage> messages = chatMessageRepository.findByChatRoomId(roomId, pageable);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow();
        ChatRoomMember chatRoomMember = chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, user).orElseThrow();

        chatRoomMember.updateLastReadAt();

        List<ChatMessageResDto> list = messages
                .getContent()
                .stream()
                .map(chatMessage ->
                    ChatMessageResDto.of(chatMessage, user.getUserId())
                )
                .toList();

        return ChatMessageSliceResDto.builder()
                .messages(list)
                .hasNext(messages.hasNext())
                .build();
    }

    @Async
    // ChatMessage 생성 (systemMessage : 파티가 생성되엇씁니다)
    public void saveSystemMessage(ChatRoom chatRoom) {
        ChatMessage message = ChatMessage.builder()
                .chatRoomId(chatRoom.getId())
                .messageType(MessageType.SYSTEM)
                .content("파티가 생성되었습니다.")
                .build();

        chatMessageRepository.save(message);

        simpMessagingTemplate.convertAndSend("/subscribe/chat/room/" + chatRoom.getId(), ChatMessageResDto.of(message));
    }
}
