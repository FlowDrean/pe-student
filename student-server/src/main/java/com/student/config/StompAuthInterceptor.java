package com.student.config;

import com.student.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

/**
 * STOMP 鉴权拦截器
 * 从 STOMP CONNECT 帧的 Authorization 头中解析 JWT，将 userId 注入为 Principal，
 * 使 convertAndSendToUser 能将消息准确路由到对应用户的 WebSocket 会话。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            if (authHeaders != null && !authHeaders.isEmpty()) {
                String bearerToken = authHeaders.get(0);
                if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                    String token = bearerToken.substring(7);
                    try {
                        if (jwtUtil.validateToken(token)) {
                            Long userId = jwtUtil.getUserIdFromToken(token);
                            Principal principal = userId::toString;
                            accessor.setUser(principal);
                            log.info("STOMP 用户 {} 已连接", userId);
                        }
                    } catch (Exception e) {
                        log.warn("STOMP CONNECT JWT 校验失败: {}", e.getMessage());
                    }
                }
            }
        }

        return message;
    }
}
