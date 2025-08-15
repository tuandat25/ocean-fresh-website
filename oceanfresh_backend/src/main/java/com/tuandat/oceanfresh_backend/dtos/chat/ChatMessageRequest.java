package com.tuandat.oceanfresh_backend.dtos.chat;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private String roomCode;
    private String content;
    private String messageType; // "TEXT", "IMAGE", "FILE"
    private String fileUrl;
}