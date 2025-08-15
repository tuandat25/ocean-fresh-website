package com.tuandat.oceanfresh_backend.repositories;

import com.tuandat.oceanfresh_backend.models.ChatRoom;
import com.tuandat.oceanfresh_backend.models.ChatRoomStatus;
import com.tuandat.oceanfresh_backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByRoomCode(String roomCode);
    
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.customer.id = :customerId AND cr.status IN ('WAITING', 'ACTIVE')")
    Optional<ChatRoom> findActiveRoomByCustomerId(@Param("customerId") Long customerId);
    
    List<ChatRoom> findByCustomerAndStatus(User customer, ChatRoomStatus status);
    
    List<ChatRoom> findByAdminAndStatus(User admin, ChatRoomStatus status);
    
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.status = 'WAITING' ORDER BY cr.createdAt ASC")
    List<ChatRoom> findWaitingRooms();
    
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.admin.id = :adminId AND cr.status = 'ACTIVE'")
    List<ChatRoom> findActiveRoomsByAdminId(@Param("adminId") Long adminId);
}