package com.tuandat.oceanfresh_backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class WebSocketSessionManager {
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final Map<String, Set<Long>> roomParticipants = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionToUser = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Thêm hoặc cập nhật session cho user
    public void addUserSession(Long userId, WebSocketSession session) {
        userSessions.put(userId, session);
        sessionToUser.put(session.getId(), userId);
        log.info("User {} connected with session {}", userId, session.getId());
    }

    // Xóa session khi disconnect
    public void removeUserSession(WebSocketSession session) {
        String sessionId = session.getId();
        Long userId = sessionToUser.remove(sessionId);
        if (userId != null) {
            userSessions.remove(userId);
            // Remove user from all rooms
            roomParticipants.values().forEach(set -> set.remove(userId));
            log.info("User {} disconnected from session {}", userId, sessionId);
        }
    }

    // Thêm user vào phòng chat
    public void addUserToRoom(String roomCode, Long userId) {
        roomParticipants.computeIfAbsent(roomCode, k -> ConcurrentHashMap.newKeySet()).add(userId);
        log.info("User {} joined room {}", userId, roomCode);
    }

    // Xóa user khỏi phòng chat
    public void removeUserFromRoom(String roomCode, Long userId) {
        Set<Long> participants = roomParticipants.get(roomCode);
        if (participants != null) {
            participants.remove(userId);
            if (participants.isEmpty()) {
                roomParticipants.remove(roomCode);
            }
        }
        log.info("User {} left room {}", userId, roomCode);
    }

    // Lấy danh sách userId trong phòng
    public Set<Long> getRoomParticipants(String roomCode) {
        return roomParticipants.getOrDefault(roomCode, Set.of());
    }

    // Lấy session theo userId
    public WebSocketSession getSession(Long userId) {
        return userSessions.get(userId);
    }

    // Kiểm tra user online
    public boolean isUserOnline(Long userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }

    // Gửi message cho tất cả user trong phòng (không loại trừ ai)
    public void sendMessageToRoom(String roomCode, Object message) {
        Set<Long> participants = getRoomParticipants(roomCode);
        for (Long userId : participants) {
            WebSocketSession session = getSession(userId);
            if (session != null && session.isOpen()) {
                try {
                    synchronized (session) {
                        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
                    }
                } catch (Exception e) {
                    log.error("Error sending message to user {}: {}", userId, e.getMessage());
                    userSessions.remove(userId);
                }
            }
        }
    }

    // // Gửi message cho tất cả user trong phòng, ngoại trừ 1 user (nếu cần)
    // public void sendMessageToRoom(String roomCode, Object message, Long
    // excludeUserId) {
    // Set<Long> participants = getRoomParticipants(roomCode);
    // for (Long userId : participants) {
    // if (excludeUserId != null && userId.equals(excludeUserId)) continue;
    // WebSocketSession session = getSession(userId);
    // if (session != null && session.isOpen()) {
    // try {
    // synchronized (session) {
    // session.sendMessage(new
    // TextMessage(objectMapper.writeValueAsString(message)));
    // }
    // } catch (Exception e) {
    // log.error("Error sending message to user {}: {}", userId, e.getMessage());
    // userSessions.remove(userId);
    // }
    // }
    // }
    // }

    // Gửi message cho 1 user cụ thể
    public boolean sendMessageToUser(Long userId, Object message) {
        WebSocketSession session = getSession(userId);
        if (session != null && session.isOpen()) {
            try {
                synchronized (session) {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
                }
                return true;
            } catch (Exception e) {
                log.error("Error sending message to user {}: {}", userId, e.getMessage());
                userSessions.remove(userId);
                return false;
            }
        }
        return false;
    }

    // Lấy danh sách userId online trong phòng
    public Set<Long> getOnlineUsersInRoom(String roomCode) {
        Set<Long> participants = roomParticipants.get(roomCode);
        if (participants == null)
            return Set.of();
        return participants.stream()
                .filter(this::isUserOnline)
                .collect(java.util.stream.Collectors.toSet());
    }
}