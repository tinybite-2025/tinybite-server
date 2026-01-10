package ita.tinybite.domain.chat.controller;

import ita.tinybite.domain.chat.dto.req.ChatMessageReqDto;
import ita.tinybite.domain.chat.dto.res.ChatMessageResDto;
import ita.tinybite.domain.chat.dto.res.ChatMessageSliceResDto;
import ita.tinybite.domain.chat.entity.ChatMessage;
import ita.tinybite.domain.chat.service.ChatService;
import ita.tinybite.global.response.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import static ita.tinybite.global.response.APIResponse.*;


@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatService chatService;

    /**
     * 1. ws://{server name}/publish/send 으로 요청 <br>
     * 2. message 저장 <br>
     * 3. 해당 채팅방을 구독중인 세션에게 해당 메시지 전달 <br>
     * 4. 만약 해당 채팅방을 구독중이지 않다면 (채팅방을 나간상태), 알림을 보냄 <br>
     */
    @MessageMapping("/send")
    public void sendMessage(ChatMessageReqDto req,
                            SimpMessageHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");

        // message entity 생성
        ChatMessage message = ChatMessage.builder()
                .messageType(req.messageType())
                .chatRoomId(req.chatRoomId())
                .senderId(userId)
                .senderName(req.nickname())
                .text(req.text())
                .imageUrl(req.imageUrl())
                .systemMessage(req.systemMessage())
                .build();

        // message 저장
        ChatMessage saved = chatService.saveMessage(message);

        // subscribe 한 사용자에게 전송
        simpMessagingTemplate.convertAndSend("/subscribe/chat/room/" + saved.getChatRoomId(), ChatMessageResDto.of(saved, userId));

        // subscribe하지 않았으나, 채팅방에 존재하는 사람들에게 전송
        chatService.sendNotification(saved, req.chatRoomId());
    }

    @ResponseBody
    @GetMapping("/api/v1/chat/{chatRoomId}")
    public APIResponse<ChatMessageSliceResDto> getChatMessages(@PathVariable Long chatRoomId,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "20") int size) {
        return success(chatService.getChatMessage(chatRoomId, page, size));
    }
}
