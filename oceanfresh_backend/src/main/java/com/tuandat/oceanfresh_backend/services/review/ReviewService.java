package com.tuandat.oceanfresh_backend.services.review;

import com.tuandat.oceanfresh_backend.dtos.review.ReviewDTO;
import com.tuandat.oceanfresh_backend.dtos.review.UpdateReviewDTO;
import com.tuandat.oceanfresh_backend.exceptions.DuplicateResourceException;
import com.tuandat.oceanfresh_backend.exceptions.ResourceNotFoundException;
import com.tuandat.oceanfresh_backend.models.*;
import com.tuandat.oceanfresh_backend.repositories.*;
import com.tuandat.oceanfresh_backend.responses.review.ProductReviewSummaryResponse;
import com.tuandat.oceanfresh_backend.responses.review.ReviewResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService implements IReviewService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderDetailRepository orderDetailRepository;
    
    @Override
    @Transactional
    public ReviewResponse createReview(ReviewDTO reviewDTO) throws Exception {
        logger.info("Creating review for product {} by user {}", reviewDTO.getProductId(), reviewDTO.getUserId());
        
        // Validate product exists
        Product product = productRepository.findById(reviewDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", "id", reviewDTO.getProductId()));
        
        // Validate user exists
        User user = userRepository.findById(reviewDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", reviewDTO.getUserId()));
        
        // Check if user already reviewed this product
        if (reviewRepository.existsByProductIdAndUserId(reviewDTO.getProductId(), reviewDTO.getUserId())) {
            throw new DuplicateResourceException("Đánh giá", "sản phẩm-người dùng", 
                    reviewDTO.getProductId() + "-" + reviewDTO.getUserId());
        }
        
        // Validate order detail if provided
        OrderDetail orderDetail = null;
        if (reviewDTO.getOrderDetailId() != null) {
            orderDetail = orderDetailRepository.findById(reviewDTO.getOrderDetailId())
                    .orElseThrow(() -> new ResourceNotFoundException("Chi tiết đơn hàng", "id", reviewDTO.getOrderDetailId()));
            
            // Verify order detail belongs to the user and product
            if (orderDetail.getOrder().getUser() == null || 
                !orderDetail.getOrder().getUser().getId().equals(reviewDTO.getUserId())) {
                throw new Exception("Đơn hàng không thuộc về người dùng này");
            }
            
            if (!orderDetail.getProductVariant().getProduct().getId().equals(reviewDTO.getProductId())) {
                throw new Exception("Chi tiết đơn hàng không phù hợp với sản phẩm");
            }
            
            // Check if already reviewed this order detail
            if (reviewRepository.existsByOrderDetailIdAndUserId(reviewDTO.getOrderDetailId(), reviewDTO.getUserId())) {
                throw new DuplicateResourceException("Đánh giá", "chi tiết đơn hàng", reviewDTO.getOrderDetailId());
            }
        }
        
        // Create review
        Review review = Review.builder()
                .product(product)
                .user(user)
                .orderDetail(orderDetail)
                .rating(reviewDTO.getRating())
                .title(reviewDTO.getTitle())
                .content(reviewDTO.getContent())
                .isApproved(true) // Auto approve by default, can be changed based on business logic
                .build();
        
        review = reviewRepository.save(review);
        logger.info("Created review with ID: {}", review.getId());
        
        return buildReviewResponse(review, reviewDTO.getUserId());
    }
    
    @Override
    @Transactional
    public ReviewResponse updateReview(Long reviewId, UpdateReviewDTO updateReviewDTO, Long userId) throws Exception {
        logger.info("Updating review {} by user {}", reviewId, userId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá", "id", reviewId));
        
        // Check if user owns this review
        if (!review.getUser().getId().equals(userId)) {
            throw new Exception("Bạn không có quyền sửa đánh giá này");
        }
        
        // Update fields if provided
        if (updateReviewDTO.getRating() != null) {
            review.setRating(updateReviewDTO.getRating());
        }
        if (updateReviewDTO.getTitle() != null) {
            review.setTitle(updateReviewDTO.getTitle());
        }
        if (updateReviewDTO.getContent() != null) {
            review.setContent(updateReviewDTO.getContent());
        }
        
        // Reset approval status when updated (optional based on business logic)
        review.setIsApproved(true);
        review.setAdminResponses(null);
        
        review = reviewRepository.save(review);
        logger.info("Updated review with ID: {}", review.getId());
        
        return buildReviewResponse(review, userId);
    }
    
    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) throws Exception {
        logger.info("Deleting review {} by user {}", reviewId, userId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá", "id", reviewId));
        
        // Check if user owns this review
        if (!review.getUser().getId().equals(userId)) {
            throw new Exception("Bạn không có quyền xóa đánh giá này");
        }
        
        reviewRepository.delete(review);
        logger.info("Deleted review with ID: {}", reviewId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(Long reviewId) throws Exception {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá", "id", reviewId));
        
        return buildReviewResponse(review, null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByProductId(Long productId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByProductId(productId, pageable);
        return reviews.map(review -> buildReviewResponse(review, null));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getApprovedReviewsByProductId(Long productId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findApprovedReviewsByProductId(productId, pageable);
        return reviews.map(review -> buildReviewResponse(review, null));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByUserId(Long userId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByUserId(userId, pageable);
        return reviews.map(review -> buildReviewResponse(review, userId));
    }
    
    @Override
    @Transactional(readOnly = true)
    public ProductReviewSummaryResponse getProductReviewSummary(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", "id", productId));
        
        Double averageRating = reviewRepository.calculateAverageRatingByProductId(productId);
        Long totalReviews = reviewRepository.countApprovedReviewsByProductId(productId);
        
        // Get rating distribution
        List<Object[]> ratingData = reviewRepository.countReviewsByRatingForProduct(productId);
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        
        // Initialize all ratings with 0
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, 0L);
        }
        
        // Fill with actual data
        for (Object[] data : ratingData) {
            Integer rating = (Integer) data[0];
            Long count = (Long) data[1];
            ratingDistribution.put(rating, count);
        }
        
        return ProductReviewSummaryResponse.builder()
                .productId(productId)
                .productName(product.getName())
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalReviews(totalReviews != null ? totalReviews : 0L)
                .ratingDistribution(ratingDistribution)
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canUserReviewProduct(Long userId, Long productId) {
        // Check if user already reviewed this product
        if (reviewRepository.existsByProductIdAndUserId(productId, userId)) {
            return false;
        }
        
        // TODO: Add additional business logic
        // e.g., check if user has purchased the product
        
        return true;
    }
    
    @Override
    @Transactional
    public ReviewResponse approveReview(Long reviewId, String adminResponse) throws Exception {
        logger.info("Approving review {}", reviewId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá", "id", reviewId));
        
        review.setIsApproved(true);
        if (adminResponse != null && !adminResponse.trim().isEmpty()) {
            review.setAdminResponses(adminResponse);
        }
        
        review = reviewRepository.save(review);
        logger.info("Approved review with ID: {}", review.getId());
        
        return buildReviewResponse(review, null);
    }
    
    @Override
    @Transactional
    public ReviewResponse rejectReview(Long reviewId, String adminResponse) throws Exception {
        logger.info("Rejecting review {}", reviewId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá", "id", reviewId));
        
        review.setIsApproved(false);
        if (adminResponse != null && !adminResponse.trim().isEmpty()) {
            review.setAdminResponses(adminResponse);
        }
        
        review = reviewRepository.save(review);
        logger.info("Rejected review with ID: {}", review.getId());
        
        return buildReviewResponse(review, null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getPendingReviews(Pageable pageable) {
        Page<Review> reviews = reviewRepository.findPendingReviews(pageable);
        return reviews.map(review -> buildReviewResponse(review, null));
    }
    
    // Helper method to build ReviewResponse
    private ReviewResponse buildReviewResponse(Review review, Long currentUserId) {
        User user = review.getUser();
        Product product = review.getProduct();
        
        boolean canEdit = currentUserId != null && currentUserId.equals(user.getId());
        boolean isVerifiedPurchase = review.getOrderDetail() != null;
        
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(product.getId())
                .productName(product.getName())
                .userId(user.getId())
                .userName(user.getFullName())
                .userAvatar(null) // TODO: Add avatar field to User model if needed
                .orderDetailId(review.getOrderDetail() != null ? review.getOrderDetail().getId() : null)
                .rating(review.getRating())
                .title(review.getTitle())
                .content(review.getContent())
                .isApproved(review.getIsApproved())
                .adminResponses(review.getAdminResponses())
                .createdAt(review.getCreatedAt().format(FORMATTER))
                .updatedAt(review.getUpdatedAt().format(FORMATTER))
                .canEdit(canEdit)
                .isVerifiedPurchase(isVerifiedPurchase)
                .build();
    }
}
