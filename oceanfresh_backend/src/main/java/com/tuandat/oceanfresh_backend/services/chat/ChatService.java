package com.tuandat.oceanfresh_backend.services.chat;

import com.tuandat.oceanfresh_backend.dtos.chat.ChatMessageRequest;
import com.tuandat.oceanfresh_backend.models.*;
import com.tuandat.oceanfresh_backend.repositories.ChatMessageRepository;
import com.tuandat.oceanfresh_backend.repositories.ChatRoomRepository;
import com.tuandat.oceanfresh_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRoomRepository chatRoomRepo;
    private final ChatMessageRepository chatMsgRepo;
    private final UserRepository userRepo;

    public boolean hasRoomAccess(String roomCode, Long userId) {
        ChatRoom room = chatRoomRepo.findByRoomCode(roomCode).orElse(null);
        if (room == null) return false;
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return false;
        if ("ADMIN".equalsIgnoreCase(user.getRole().getName())) return true;
        return room.getCustomer() != null && room.getCustomer().getId().equals(userId);
    }

    @Transactional
    public ChatMessage saveMessage(ChatMessageRequest req, Long senderId) {
        ChatRoom room = chatRoomRepo.findByRoomCode(req.getRoomCode())
                .orElseThrow(() -> new IllegalArgumentException("Phòng chat không tồn tại"));
        User sender = userRepo.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Người gửi không tồn tại"));

        // Xác định SenderType dựa trên role
        SenderType senderType = "ADMIN".equalsIgnoreCase(sender.getRole().getName()) 
            ? SenderType.ADMIN : SenderType.USER;
        
        // Xác định MessageType
        MessageType messageType = MessageType.TEXT; // Default
        if (req.getMessageType() != null) {
            try {
                messageType = MessageType.valueOf(req.getMessageType().toUpperCase());
            } catch (IllegalArgumentException e) {
                messageType = MessageType.TEXT;
            }
        }

        ChatMessage message = ChatMessage.builder()
                .room(room)
                .sender(sender)
                .senderType(senderType)
                .messageType(messageType)
                .content(req.getContent())
                .fileUrl(req.getFileUrl())
                .isRead(false)
                .sentAt(LocalDateTime.now())
                .build();

        return chatMsgRepo.save(message);
    }

    public List<ChatMessage> getMessageHistory(String roomCode, Long userId, int page, int size) {
        if (!hasRoomAccess(roomCode, userId)) {
            throw new IllegalArgumentException("Không có quyền truy cập phòng chat này");
        }
        
        ChatRoom room = chatRoomRepo.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Phòng chat không tồn tại"));

        return chatMsgRepo.findByRoomOrderBySentAtAsc(room,
            org.springframework.data.domain.PageRequest.of(page, size))
            .getContent();
    }

    @Transactional
    public ChatRoom createOrGetRoom(Long customerId, Long adminId) {
        Optional<ChatRoom> existingRoom = chatRoomRepo.findActiveRoomByCustomerId(customerId);
        
        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }
        
        User customer = userRepo.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Khách hàng không tồn tại"));
        User admin = adminId != null ? userRepo.findById(adminId).orElse(null) : null;
        
        String roomCode = generateRoomCode();
        
        ChatRoom newRoom = ChatRoom.builder()
                .roomCode(roomCode)
                .customer(customer)
                .admin(admin)
                .status(admin != null ? ChatRoomStatus.ACTIVE : ChatRoomStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .build();
                
        return chatRoomRepo.save(newRoom);
    }

    @Transactional
    public void markMessagesAsRead(String roomCode, Long userId) {
        ChatRoom room = chatRoomRepo.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Phòng chat không tồn tại"));
                
        chatMsgRepo.markMessagesAsReadForUser(room.getId(), userId);
    }

    private String generateRoomCode() {
        return "ROOM_" + System.currentTimeMillis() + "_" + 
               (int)(Math.random() * 10000);
    }
}