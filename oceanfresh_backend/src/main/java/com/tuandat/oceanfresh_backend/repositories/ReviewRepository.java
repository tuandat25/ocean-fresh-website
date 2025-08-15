package com.tuandat.oceanfresh_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tuandat.oceanfresh_backend.models.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    /**
     * Tìm tất cả reviews của một sản phẩm
     */
    List<Review> findByProductId(Long productId);
    
    /**
     * Tìm tất cả reviews của một sản phẩm với phân trang
     */
    Page<Review> findByProductId(Long productId, Pageable pageable);
    
    /**
     * Tìm tất cả reviews của một user
     */
    List<Review> findByUserId(Long userId);
    
    /**
     * Tìm tất cả reviews của một user với phân trang
     */
    Page<Review> findByUserId(Long userId, Pageable pageable);
    
    /**
     * Tìm reviews đã được duyệt của một sản phẩm
     */
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.isApproved = true ORDER BY r.createdAt DESC")
    List<Review> findApprovedReviewsByProductId(@Param("productId") Long productId);
    
    /**
     * Tìm reviews đã được duyệt của một sản phẩm với phân trang
     */
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.isApproved = true ORDER BY r.createdAt DESC")
    Page<Review> findApprovedReviewsByProductId(@Param("productId") Long productId, Pageable pageable);
    
    /**
     * Tìm reviews theo rating của một sản phẩm
     */
    List<Review> findByProductIdAndRating(Long productId, Integer rating);
    
    /**
     * Kiểm tra user đã review sản phẩm chưa
     */
    boolean existsByProductIdAndUserId(Long productId, Long userId);
    
    /**
     * Kiểm tra user đã review sản phẩm từ order detail cụ thể chưa
     */
    boolean existsByOrderDetailIdAndUserId(Long orderDetailId, Long userId);
    
    /**
     * Tìm review của user cho sản phẩm cụ thể
     */
    Optional<Review> findByProductIdAndUserId(Long productId, Long userId);
    
    /**
     * Tìm review từ order detail cụ thể
     */
    Optional<Review> findByOrderDetailId(Long orderDetailId);
    
    /**
     * Tính rating trung bình của một sản phẩm
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.isApproved = true")
    Double calculateAverageRatingByProductId(@Param("productId") Long productId);
    
    /**
     * Đếm số lượng reviews của một sản phẩm
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.isApproved = true")
    Long countApprovedReviewsByProductId(@Param("productId") Long productId);
    
    /**
     * Tìm reviews chưa được duyệt
     */
    @Query("SELECT r FROM Review r WHERE r.isApproved = false ORDER BY r.createdAt DESC")
    Page<Review> findPendingReviews(Pageable pageable);
    
    /**
     * Thống kê reviews theo rating cho một sản phẩm
     */
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.isApproved = true GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> countReviewsByRatingForProduct(@Param("productId") Long productId);
}
