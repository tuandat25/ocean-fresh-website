package com.tuandat.oceanfresh_backend.controllers;

import com.tuandat.oceanfresh_backend.dtos.chat.ChatMessageRequest;
import com.tuandat.oceanfresh_backend.dtos.chat.ChatMessageResponse;
import com.tuandat.oceanfresh_backend.models.ChatMessage;
import com.tuandat.oceanfresh_backend.models.ChatRoom;
import com.tuandat.oceanfresh_backend.models.User;
import com.tuandat.oceanfresh_backend.responses.ApiResponse;
import com.tuandat.oceanfresh_backend.responses.websocket.ChatRoomResponse;
import com.tuandat.oceanfresh_backend.services.chat.ChatService;
import com.tuandat.oceanfresh_backend.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/chat")
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    private final WebSocketSessionManager sessionManager;

    @PostMapping("/rooms")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createRoom(
            @RequestParam(required = false) Long adminId) {
        try {
            Long customerId = getCurrentUserId();
            ChatRoom room = chatService.createOrGetRoom(customerId, adminId);
            
            ChatRoomResponse response = ChatRoomResponse.fromEntity(room);
            
            return ResponseEntity.ok(ApiResponse.<ChatRoomResponse>builder()
                    .success(true)
                    .message("Phòng chat đã được tạo thành công")
                    .data(response)
                    .build());
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<ChatRoomResponse>builder()
                    .success(false)
                    .message("Lỗi tạo phòng chat: " + e.getMessage())
                    .build());
        }
    }

    @GetMapping("/rooms/{roomCode}/messages")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
            @PathVariable String roomCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "500") int size) {
        try {
            Long userId = getCurrentUserId();
            List<ChatMessage> messages = chatService.getMessageHistory(roomCode, userId, page, size);
            
            List<ChatMessageResponse> responses = messages.stream()
                    .map(ChatMessageResponse::fromEntity)
                    .collect(Collectors.toList());
                    
            return ResponseEntity.ok(ApiResponse.<List<ChatMessageResponse>>builder()
                    .success(true)
                    .message("Lấy lịch sử tin nhắn thành công")
                    .data(responses)
                    .build());
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<List<ChatMessageResponse>>builder()
                    .success(false)
                    .message("Lỗi lấy tin nhắn: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/rooms/{roomCode}/messages")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @PathVariable String roomCode,
            @RequestBody ChatMessageRequest request) {
        try {
            Long senderId = getCurrentUserId();
            request.setRoomCode(roomCode);
            
            ChatMessage savedMessage = chatService.saveMessage(request, senderId);
            ChatMessageResponse response = ChatMessageResponse.fromEntity(savedMessage);
            
            // Add user to room and send via WebSocket
            sessionManager.addUserToRoom(roomCode, senderId);
            sessionManager.sendMessageToRoom(roomCode, response);
            
            return ResponseEntity.ok(ApiResponse.<ChatMessageResponse>builder()
                    .success(true)
                    .message("Tin nhắn đã được gửi thành công")
                    .data(response)
                    .build());
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<ChatMessageResponse>builder()
                    .success(false)
                    .message("Lỗi gửi tin nhắn: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/rooms/{roomCode}/messages/mark-read")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> markMessagesAsRead(@PathVariable String roomCode) {
        try {
            Long userId = getCurrentUserId();
            chatService.markMessagesAsRead(roomCode, userId);
            
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .success(true)
                    .message("Đã đánh dấu tin nhắn là đã đọc")
                    .data("success")
                    .build());
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .success(false)
                    .message("Lỗi đánh dấu tin nhắn: " + e.getMessage())
                    .build());
        }
    }

    @GetMapping("/rooms/{roomCode}/participants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRoomParticipants(@PathVariable String roomCode) {
        try {
            Long userId = getCurrentUserId();
            if (!chatService.hasRoomAccess(roomCode, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.<Map<String, Object>>builder()
                        .success(false)
                        .message("Không có quyền truy cập phòng chat này")
                        .build());
            }
            
            var onlineUsers = sessionManager.getOnlineUsersInRoom(roomCode);
            var allParticipants = sessionManager.getRoomParticipants(roomCode);
            
            Map<String, Object> result = Map.of(
                "onlineUsers", onlineUsers,
                "totalParticipants", allParticipants.size(),
                "onlineCount", onlineUsers.size()
            );
            
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("Lấy thông tin người tham gia thành công")
                    .data(result)
                    .build());
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("Lỗi lấy thông tin người tham gia: " + e.getMessage())
                    .build());
        }
    }

    @GetMapping("/rooms/my-rooms")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getMyRooms() {
        try {
            // This would require additional methods in ChatService
            // For now, return empty list with success message
            return ResponseEntity.ok(ApiResponse.<List<ChatRoomResponse>>builder()
                    .success(true)
                    .message("Lấy danh sách phòng chat thành công")
                    .data(List.of())
                    .build());
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<List<ChatRoomResponse>>builder()
                    .success(false)
                    .message("Lỗi lấy danh sách phòng chat: " + e.getMessage())
                    .build());
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return ((User) authentication.getPrincipal()).getId();
        }
        throw new RuntimeException("Không thể xác định người dùng hiện tại");
    }
}
