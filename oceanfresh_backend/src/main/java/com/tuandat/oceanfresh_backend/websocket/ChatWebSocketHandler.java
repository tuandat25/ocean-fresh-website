package com.tuandat.oceanfresh_backend.websocket;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuandat.oceanfresh_backend.components.JwtTokenUtils;
import com.tuandat.oceanfresh_backend.dtos.chat.ChatMessageRequest;
import com.tuandat.oceanfresh_backend.dtos.chat.ChatMessageResponse;
import com.tuandat.oceanfresh_backend.models.ChatMessage;
import com.tuandat.oceanfresh_backend.services.chat.ChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final WebSocketSessionManager sessionManager;
    private final ChatService chatService;
    private final ObjectMapper objectMapper;
    private final JwtTokenUtils jwtTokenUtils;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            Long userId = extractUserIdFromSession(session);
            String roomCode = getRoomCodeFromSession(session); // Lấy roomCode từ query param hoặc attribute
            if (userId != null) {
                sessionManager.addUserSession(userId, session);
                sessionManager.addUserToRoom(roomCode, userId);

                // Gửi thông báo kết nối thành công
                Map<String, Object> welcomeMessage = new HashMap<>();
                welcomeMessage.put("type", "CONNECTION_SUCCESS");
                welcomeMessage.put("message", "Kết nối thành công");
                welcomeMessage.put("userId", userId);

                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(welcomeMessage)));
                log.info("User {} connected successfully", userId);
            } else {
                log.warn("Invalid token, closing connection");
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token"));
            }
        } catch (Exception e) {
            log.error("Error in connection establishment: {}", e.getMessage());
            try {
                session.close(CloseStatus.SERVER_ERROR);
            } catch (Exception ignored) {
            }
        }
    }

    private String getRoomCodeFromSession(WebSocketSession session) {
        if (session.getUri() == null)
            return null;
        String query = session.getUri().getQuery(); // chỉ lấy phần query string
        if (query == null)
            return null;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2 && pair[0].equals("roomCode")) {
                return pair[1];
            }
        }
        return null;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionManager.removeUserSession(session);
        log.info("WebSocket connection closed: {}", status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            Long senderId = extractUserIdFromSession(session);
            if (senderId == null) {
                sendErrorMessage(session, "Phiên đăng nhập không hợp lệ");
                return;
            }

            ChatMessageRequest req = objectMapper.readValue(message.getPayload(), ChatMessageRequest.class);

            // Validate room access
            if (!chatService.hasRoomAccess(req.getRoomCode(), senderId)) {
                sendErrorMessage(session, "Không có quyền truy cập phòng chat này");
                return;
            }

            // Save message
            ChatMessage savedMessage = chatService.saveMessage(req, senderId);

            // Convert to response DTO
            ChatMessageResponse responseDto = ChatMessageResponse.fromEntity(savedMessage);

            // Add user to room if not already added
            sessionManager.addUserToRoom(req.getRoomCode(), senderId);

            // Broadcast cho tất cả thành viên trong phòng, bao gồm cả người gửi
            // sessionManager.sendMessageToRoom(req.getRoomCode(), responseDto, null);
            sessionManager.sendMessageToRoom(req.getRoomCode(), responseDto); // Không truyền excludeUserId
            // Nếu vẫn muốn gửi xác nhận riêng cho người gửi, giữ lại đoạn này:
            Map<String, Object> deliveryConfirm = new HashMap<>();
            deliveryConfirm.put("type", "MESSAGE_DELIVERED");
            deliveryConfirm.put("messageId", savedMessage.getId());
            deliveryConfirm.put("roomCode", req.getRoomCode());

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(deliveryConfirm)));

        } catch (Exception e) {
            log.error("Error handling message: {}", e.getMessage(), e);
            sendErrorMessage(session, "Lỗi xử lý tin nhắn: " + e.getMessage());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Transport error for session {}: {}", session.getId(), exception.getMessage());
        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (Exception ignored) {
        }
    }

    private Long extractUserIdFromSession(WebSocketSession session) {
        // Lấy userId từ attribute đã được xác thực ở WebSocketAuthInterceptor
        Object userIdAttr = session.getAttributes().get("userId");
        if (userIdAttr instanceof Long) {
            return (Long) userIdAttr;
        }
        if (userIdAttr instanceof Integer) {
            return ((Integer) userIdAttr).longValue();
        }
        return null;
    }

    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        try {
            Map<String, Object> error = new HashMap<>();
            error.put("type", "ERROR");
            error.put("message", errorMessage);
            error.put("timestamp", System.currentTimeMillis());

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
        } catch (Exception e) {
            log.error("Error sending error message: {}", e.getMessage());
        }
    }
}