package ita.tinybite.domain.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

/**
 * subscribe, unsubscribe, disconnect 이벤트 발생 시 registry에 자동으로 값을 변경하는 스프링 빈 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatEventListener {

    private final ChatSubscribeRegistry registry;

    /**
     * 구독 시, 호출되는 메서드 <br>
     * 1. StompHeader에서 userId, destination, sessionId, subscriptionId 받아옴 (destination 예 : /subscribe/chat/room/{chatRoomId}) <br>
     * 2. destination에서 roomId resolve <br>
     * 3. 이후 세션정보를 registry에 등록
     */
    @EventListener
    public void onSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(event.getMessage());

        Long userId = (Long) acc.getSessionAttributes().get("userId");
        String destination = acc.getDestination();
        String sessionId = acc.getSessionId();
        String subscriptionId = acc.getSubscriptionId();

        if (userId == null || destination == null) return;

        // /subscribe/chat/room/1
        Long roomId = extractRoomId(destination);
        registry.register(sessionId, subscriptionId, roomId, userId);
    }

    /**
     * 구독취소시 호출되는 메서드 <br>
     * 1. StompHeader에서 sessionId, subscriptionId resolve <br>
     * 2. 해당 구독정보를 가지고 있는 사용자를 registry에서 삭제 <br>
     * 3. 만약 네트워크 에러 등으로 유저 정보가 없을 시, 유저에 해당하는 구독 정보를 삭제
     */
    @EventListener
    public void onUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = acc.getSessionId();
        String subscriptionId = acc.getSubscriptionId();

        if (sessionId != null && subscriptionId != null) {
            registry.unregister(sessionId, subscriptionId);
        }
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("[STOMP] DISCONNECT sessionId={}", accessor.getSessionId());
        log.info("[STOMP] headers={}", accessor.toNativeHeaderMap());

        StompHeaderAccessor acc = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = acc.getSessionId();
        if (sessionId != null) {
            registry.unregisterSession(sessionId);
            return;
        }

        Long userId = acc.getSessionAttributes() != null ? (Long) acc.getSessionAttributes().get("userId") : null;
        if (userId != null) {
            registry.removeUserEverywhere(userId);
        }
    }

    private Long extractRoomId(String destination) {
        // /subscribe/chat/room/{roomId}
        try {
            return Long.parseLong(destination.substring("/subscribe/chat/room/".length()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @EventListener
    public void onConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("[STOMP] CONNECT sessionId={}", accessor.getSessionId());
        log.info("[STOMP] headers={}", accessor.toNativeHeaderMap());
    }

    @EventListener
    public void onConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("[STOMP] CONNECTED sessionId={}", accessor.getSessionId());
    }
}
