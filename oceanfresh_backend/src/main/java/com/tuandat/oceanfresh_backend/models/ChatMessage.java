package com.tuandat.oceanfresh_backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private ChatRoom room;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SenderType senderType; // USER hoặc ADMIN

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType messageType; // TEXT, IMAGE, FILE

    @Column(columnDefinition = "TEXT")
    private String content;

    private String fileUrl;
    private Boolean isRead;
    private LocalDateTime sentAt;
}