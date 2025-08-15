package com.tuandat.oceanfresh_backend.repositories;

import com.tuandat.oceanfresh_backend.models.ChatMessage;
import com.tuandat.oceanfresh_backend.models.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRoomIdOrderBySentAtAsc(Long roomId);
    
    Page<ChatMessage> findByRoomOrderBySentAtDesc(ChatRoom room, Pageable pageable);
    Page<ChatMessage> findByRoomOrderBySentAtAsc(ChatRoom room, Pageable pageable);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.room.id = :roomId ORDER BY cm.sentAt DESC")
    List<ChatMessage> findByRoomIdOrderBySentAtDesc(@Param("roomId") Long roomId);
    
    @Modifying
    @Query("UPDATE ChatMessage cm SET cm.isRead = true WHERE cm.room.id = :roomId AND cm.sender.id != :userId AND cm.isRead = false")
    void markMessagesAsReadForUser(@Param("roomId") Long roomId, @Param("userId") Long userId);
    
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.room.id = :roomId AND cm.isRead = false AND cm.sender.id != :userId")
    Long countUnreadMessages(@Param("roomId") Long roomId, @Param("userId") Long userId);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.room.roomCode = :roomCode ORDER BY cm.sentAt ASC")
    List<ChatMessage> findByRoomCodeOrderBySentAtAsc(@Param("roomCode") String roomCode);
}
