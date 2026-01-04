package ita.tinybite.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

/**
 * subscribe, unsubscribe, disconnect 이벤트 발생 시 registry에 자동으로 값을 변경하는 스프링 빈 클래스
 */
@Component
@RequiredArgsConstructor
public class ChatEventListener {

    private final ChatSubscribeRegistry registry;

    @EventListener
    public void onSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(event.getMessage());

        Long userId = (Long) acc.getSessionAttributes().get("userId");
        String destination = acc.getDestination();

        if (userId == null || destination == null) return;

        // /subscribe/chat/room/1
        Long roomId = extractRoomId(destination);
        if (roomId != null) {
            registry.add(roomId, userId);
        }
    }

    @EventListener
    public void onUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(event.getMessage());

        Long userId = (Long) acc.getSessionAttributes().get("userId");
        String destination = acc.getDestination();

        if (userId == null || destination == null) return;

        Long roomId = extractRoomId(destination);
        if (roomId != null) {
            registry.remove(roomId, userId);
        }
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(event.getMessage());

        Long userId = (Long) acc.getSessionAttributes().get("userId");
        if (userId != null) {
            registry.removeUserEverywhere(userId);
        }
    }

    private Long extractRoomId(String destination) {
        // /subscribe/chat/room/{roomId}
        if (!destination.startsWith("/subscribe/chat/room/")) return null;
        try {
            return Long.parseLong(destination.substring("/subscribe/chat/room/".length()));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

