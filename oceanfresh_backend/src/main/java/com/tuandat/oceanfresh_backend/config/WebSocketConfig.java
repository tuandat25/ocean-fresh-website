package com.tuandat.oceanfresh_backend.config;

import com.tuandat.oceanfresh_backend.websocket.ChatWebSocketHandler;
import com.tuandat.oceanfresh_backend.websocket.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    // @Override
    // public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    // log.info("=== Registering WebSocket Handlers ===");

    // registry.addHandler(chatWebSocketHandler, "/ws/chat")
    // .addInterceptors(webSocketAuthInterceptor)
    // .setAllowedOrigins("*")
    // .withSockJS();

    // log.info("✅ WebSocket handler registered at: /ws/chat");
    // }
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("=== Registering WebSocket Handlers ===");
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins("*");
        log.info("✅ WebSocket handler registered at: /ws/chat");
    }
}