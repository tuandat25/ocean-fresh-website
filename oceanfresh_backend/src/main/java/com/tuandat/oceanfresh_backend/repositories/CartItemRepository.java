package com.tuandat.oceanfresh_backend.repositories;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tuandat.oceanfresh_backend.models.CartItem;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    /**
     * Tìm tất cả các mặt hàng trong giỏ hàng theo cartId
     */
    List<CartItem> findByCartId(Long cartId);
    
    /**
     * Tìm mặt hàng trong giỏ hàng theo cartId và productVariantId
     */
    Optional<CartItem> findByCartIdAndProductVariantId(Long cartId, Long productVariantId);
    
    /**
     * Tìm tất cả các mặt hàng trong giỏ hàng của user
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.userId = :userId")
    List<CartItem> findByUserId(@Param("userId") Long userId);
    
    /**
     * Tìm tất cả các mặt hàng trong giỏ hàng theo sessionId
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.sessionId = :sessionId")
    List<CartItem> findBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * Đếm số lượng mặt hàng trong giỏ hàng
     */
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Long countByCartId(@Param("cartId") Long cartId);
    
    /**
     * Tính tổng tiền của giỏ hàng
     */
    @Query("SELECT SUM(ci.quantity * ci.priceAtAddition) FROM CartItem ci WHERE ci.cart.id = :cartId")
    BigDecimal calculateTotalByCartId(@Param("cartId") Long cartId);
    
    /**
     * Tính tổng số lượng sản phẩm trong giỏ hàng
     */
    @Query("SELECT SUM(ci.quantity) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Integer calculateTotalQuantityByCartId(@Param("cartId") Long cartId);
    
    /**
     * Kiểm tra xem sản phẩm variant đã có trong giỏ hàng chưa
     */
    boolean existsByCartIdAndProductVariantId(Long cartId, Long productVariantId);
    
    /**
     * Xóa tất cả mặt hàng trong giỏ hàng
     */
    void deleteByCartId(Long cartId);
    
    /**
     * Xóa mặt hàng cụ thể trong giỏ hàng
     */
    void deleteByCartIdAndProductVariantId(Long cartId, Long productVariantId);
    
    /**
     * Tìm các mặt hàng theo user hoặc session với thông tin đầy đủ
     */
    @Query("SELECT ci FROM CartItem ci " +
           "JOIN FETCH ci.productVariant pv " +
           "JOIN FETCH pv.product p " +
           "WHERE (:userId IS NOT NULL AND ci.cart.userId = :userId) OR " +
           "(:sessionId IS NOT NULL AND ci.cart.sessionId = :sessionId) " +
           "ORDER BY ci.createdAt DESC")
    List<CartItem> findCartItemsWithDetailsForUserOrSession(@Param("userId") Long userId, 
                                                           @Param("sessionId") String sessionId);
}
