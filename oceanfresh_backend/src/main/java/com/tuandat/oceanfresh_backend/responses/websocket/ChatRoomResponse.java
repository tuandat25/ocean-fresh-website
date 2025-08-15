package com.tuandat.oceanfresh_backend.responses.websocket;

import java.time.LocalDateTime;
import java.util.List;

import com.tuandat.oceanfresh_backend.models.ChatRoom;
import com.tuandat.oceanfresh_backend.models.ChatRoomStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {
    private Long id;
    private String roomCode;
    private Long customerId;
    private String customerName;
    private Long adminId;
    private String adminName;
    private ChatRoomStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
    private List<ChatMessageResponse> recentMessages;
    private int unreadCount;

    public static ChatRoomResponse fromEntity(ChatRoom room) {
        return ChatRoomResponse.builder()
                .id(room.getId())
                .roomCode(room.getRoomCode())
                .customerId(room.getCustomer() != null ? room.getCustomer().getId() : null)
                .customerName(room.getCustomer() != null ? room.getCustomer().getFullName() : null)
                .adminId(room.getAdmin() != null ? room.getAdmin().getId() : null)
                .adminName(room.getAdmin() != null ? room.getAdmin().getFullName() : null)
                .status(room.getStatus())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .closedAt(room.getClosedAt())
                .unreadCount(0) // This would need to be calculated separately
                .build();
    }
}
