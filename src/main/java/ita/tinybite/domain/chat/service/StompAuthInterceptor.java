package ita.tinybite.domain.chat.service;

import ita.tinybite.domain.auth.entity.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * http가 아닌, 웹소켓에서 인증을 위한 인터셉터 (filter는 사용 안됨)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        log.info("StompAuthInterceptor authHeader: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        log.info("StompAuthInterceptor preSend token: {}", token);
        jwtTokenProvider.validateToken(token);
        Long userId = jwtTokenProvider.getUserId(token);
        log.info("StompAuthInterceptor preSend userId: {}", userId);
        attributes.put("userId", userId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }
}
