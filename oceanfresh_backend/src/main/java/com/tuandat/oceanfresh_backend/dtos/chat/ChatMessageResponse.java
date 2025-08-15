package com.tuandat.oceanfresh_backend.dtos.chat;

import com.tuandat.oceanfresh_backend.models.ChatMessage;
import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageResponse {
    private Long id;
    private String roomCode;
    private Long senderId;
    private String senderName;
    private String senderType;
    private String content;
    private String messageType;
    private String fileUrl;
    private Boolean isRead;
    private LocalDateTime sentAt;
    private boolean delivered;

    public static ChatMessageResponse fromEntity(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .roomCode(message.getRoom().getRoomCode())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFullName())
                .senderType(message.getSenderType().name())
                .content(message.getContent())
                .messageType(message.getMessageType().name())
                .fileUrl(message.getFileUrl())
                .isRead(message.getIsRead())
                .sentAt(message.getSentAt())
                .delivered(true)
                .build();
    }
}
