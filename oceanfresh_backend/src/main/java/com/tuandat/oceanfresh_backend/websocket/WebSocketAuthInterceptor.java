package com.tuandat.oceanfresh_backend.websocket;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.tuandat.oceanfresh_backend.components.JwtTokenUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {
    private final JwtTokenUtils jwtTokenUtils;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            String query = request.getURI().getQuery();
            log.info("=== WebSocket Handshake Attempt ===");
            log.info("Query: {}", query);
            log.info("URI: {}", request.getURI());

            String token = null;
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && pair[0].equals("token")) {
                        token = pair[1];
                        break;
                    }
                }
            }

            if (token != null) {
                log.info("Extracted token: {}...", token.substring(0, Math.min(token.length(), 20)));
                if (jwtTokenUtils.validateTokenForWebSocket(token)) {
                    Long userId = jwtTokenUtils.getUserIdFromToken(token);
                    if (userId != null) {
                        attributes.put("userId", userId);
                        log.info("✅ WebSocket handshake SUCCESS for user: {}", userId);
                        return true;
                    }
                }
                log.warn("❌ Token validation failed");
            } else {
                log.warn("❌ No token found in query");
            }

            log.warn("❌ WebSocket handshake FAILED");
            return false;

        } catch (Exception e) {
            log.error("❌ Error during WebSocket handshake: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket handshake error: {}", exception.getMessage());
        } else {
            log.info("✅ WebSocket handshake completed successfully");
        }
    }
}