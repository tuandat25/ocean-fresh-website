package com.tuandat.oceanfresh_backend.tasks;

import com.tuandat.oceanfresh_backend.models.ChatRoom;
import com.tuandat.oceanfresh_backend.models.ChatRoomStatus;
import com.tuandat.oceanfresh_backend.repositories.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatCleanupTask {
    
    private final ChatRoomRepository chatRoomRepository;

    /**
     * Cleanup inactive chat rooms every hour
     * Rooms that have been inactive for more than 2 hours will be closed
     */
    @Scheduled(fixedRate = 3600000) // Run every hour
    @Transactional
    public void cleanupInactiveRooms() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(2);
            
            List<ChatRoom> inactiveRooms = chatRoomRepository.findAll().stream()
                    .filter(room -> room.getStatus() == ChatRoomStatus.ACTIVE)
                    .filter(room -> room.getUpdatedAt() != null && room.getUpdatedAt().isBefore(cutoffTime))
                    .toList();
            
            for (ChatRoom room : inactiveRooms) {
                room.setStatus(ChatRoomStatus.CLOSED);
                room.setClosedAt(LocalDateTime.now());
                room.setUpdatedAt(LocalDateTime.now());
                chatRoomRepository.save(room);
                
                log.info("Closed inactive room: {}", room.getRoomCode());
            }
            
            if (!inactiveRooms.isEmpty()) {
                log.info("Cleaned up {} inactive chat rooms", inactiveRooms.size());
            }
            
        } catch (Exception e) {
            log.error("Error during chat room cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Cleanup old closed rooms every day
     * Remove rooms that have been closed for more than 30 days
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run every day at 2 AM
    @Transactional
    public void cleanupOldClosedRooms() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(30);
            
            List<ChatRoom> oldRooms = chatRoomRepository.findAll().stream()
                    .filter(room -> room.getStatus() == ChatRoomStatus.CLOSED)
                    .filter(room -> room.getClosedAt() != null && room.getClosedAt().isBefore(cutoffTime))
                    .toList();
            
            if (!oldRooms.isEmpty()) {
                chatRoomRepository.deleteAll(oldRooms);
                log.info("Deleted {} old closed chat rooms", oldRooms.size());
            }
            
        } catch (Exception e) {
            log.error("Error during old room cleanup: {}", e.getMessage(), e);
        }
    }
}