package com.tuandat.oceanfresh_backend.models;

import jakarta.persistence.*;
import lombok.*;
import com.tuandat.oceanfresh_backend.models.ChatRoomStatus;
import com.tuandat.oceanfresh_backend.models.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String roomCode;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private User customer;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private User admin;

    @Enumerated(EnumType.STRING)
    private ChatRoomStatus status; // WAITING, ACTIVE, CLOSED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
}