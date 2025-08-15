package com.tuandat.oceanfresh_backend.responses.websocket;

import java.time.LocalDateTime;

import com.tuandat.oceanfresh_backend.models.MessageType;
import com.tuandat.oceanfresh_backend.models.SenderType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private String roomCode;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private SenderType senderType;
    private String content;
    private MessageType messageType;
    private String fileUrl;
    private LocalDateTime sentAt;
    private boolean delivered;
    private boolean read;
}
