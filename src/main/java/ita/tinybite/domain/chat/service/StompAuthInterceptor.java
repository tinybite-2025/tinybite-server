package ita.tinybite.domain.chat.service;

import ita.tinybite.domain.auth.entity.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * http가 아닌, 웹소켓에서 인증을 위한 인터셉터 (filter는 사용 안됨)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        log.info("StompAuthInterceptor preSend");
        log.info(accessor.getCommand().toString());
        if(StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            log.info("StompAuthInterceptor authHeader: {}", authHeader);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);
            log.info("StompAuthInterceptor preSend token: {}", token);
            jwtTokenProvider.validateToken(token);
            Long userId = jwtTokenProvider.getUserId(token);
            log.info("StompAuthInterceptor preSend userId: {}", userId);
            accessor.getSessionAttributes().put("userId", userId);
        }
        return message;
    }
}
