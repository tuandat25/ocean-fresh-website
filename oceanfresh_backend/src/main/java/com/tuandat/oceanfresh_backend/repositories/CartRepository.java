package com.tuandat.oceanfresh_backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tuandat.oceanfresh_backend.models.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    /**
     * Tìm giỏ hàng theo userId
     */
    Optional<Cart> findByUserId(Long userId);
    
    /**
     * Tìm giỏ hàng theo sessionId cho khách vãng lai
     */
    Optional<Cart> findBySessionId(String sessionId);
    
    /**
     * Tìm giỏ hàng theo userId hoặc sessionId
     */
    @Query("SELECT c FROM Cart c WHERE " +
           "(:userId IS NOT NULL AND c.userId = :userId) OR " +
           "(:sessionId IS NOT NULL AND c.sessionId = :sessionId)")
    Optional<Cart> findByUserIdOrSessionId(@Param("userId") Long userId, 
                                         @Param("sessionId") String sessionId);
    
    /**
     * Kiểm tra giỏ hàng có tồn tại theo userId không
     */
    boolean existsByUserId(Long userId);
    
    /**
     * Kiểm tra giỏ hàng có tồn tại theo sessionId không
     */
    boolean existsBySessionId(String sessionId);
    
    /**
     * Xóa giỏ hàng theo userId
     */
    void deleteByUserId(Long userId);
    
    /**
     * Xóa giỏ hàng theo sessionId
     */
    void deleteBySessionId(String sessionId);
}
