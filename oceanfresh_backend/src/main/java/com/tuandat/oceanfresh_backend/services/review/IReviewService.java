package com.tuandat.oceanfresh_backend.services.review;

import com.tuandat.oceanfresh_backend.dtos.review.ReviewDTO;
import com.tuandat.oceanfresh_backend.dtos.review.UpdateReviewDTO;
import com.tuandat.oceanfresh_backend.responses.review.ProductReviewSummaryResponse;
import com.tuandat.oceanfresh_backend.responses.review.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IReviewService {
    
    /**
     * Tạo review mới
     */
    ReviewResponse createReview(ReviewDTO reviewDTO) throws Exception;
    
    /**
     * Cập nhật review
     */
    ReviewResponse updateReview(Long reviewId, UpdateReviewDTO updateReviewDTO, Long userId) throws Exception;
    
    /**
     * Xóa review
     */
    void deleteReview(Long reviewId, Long userId) throws Exception;
    
    /**
     * Lấy review theo ID
     */
    ReviewResponse getReviewById(Long reviewId) throws Exception;
    
    /**
     * Lấy tất cả reviews của một sản phẩm
     */
    Page<ReviewResponse> getReviewsByProductId(Long productId, Pageable pageable);
    
    /**
     * Lấy reviews đã được duyệt của một sản phẩm
     */
    Page<ReviewResponse> getApprovedReviewsByProductId(Long productId, Pageable pageable);
    
    /**
     * Lấy tất cả reviews của một user
     */
    Page<ReviewResponse> getReviewsByUserId(Long userId, Pageable pageable);
    
    /**
     * Lấy summary thống kê reviews của một sản phẩm
     */
    ProductReviewSummaryResponse getProductReviewSummary(Long productId);
    
    /**
     * Kiểm tra user có thể review sản phẩm không
     */
    boolean canUserReviewProduct(Long userId, Long productId);
    
    /**
     * Duyệt review (Admin)
     */
    ReviewResponse approveReview(Long reviewId, String adminResponse) throws Exception;
    
    /**
     * Từ chối review (Admin)
     */
    ReviewResponse rejectReview(Long reviewId, String adminResponse) throws Exception;
    
    /**
     * Lấy reviews chưa được duyệt (Admin)
     */
    Page<ReviewResponse> getPendingReviews(Pageable pageable);
}
