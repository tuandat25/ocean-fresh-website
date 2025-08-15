package com.tuandat.oceanfresh_backend.controllers;

import com.tuandat.oceanfresh_backend.dtos.review.ReviewDTO;
import com.tuandat.oceanfresh_backend.dtos.review.UpdateReviewDTO;
import com.tuandat.oceanfresh_backend.models.User;
import com.tuandat.oceanfresh_backend.responses.review.ProductReviewSummaryResponse;
import com.tuandat.oceanfresh_backend.responses.review.ReviewResponse;
import com.tuandat.oceanfresh_backend.services.review.IReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/reviews")
@RequiredArgsConstructor
public class ReviewController {
    
    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);
    private final IReviewService reviewService;
    
    @PostMapping("")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createReview(@Valid @RequestBody ReviewDTO reviewDTO) {
        try {
            User user = getCurrentUser();
            reviewDTO.setUserId(user.getId());
            
            ReviewResponse reviewResponse = reviewService.createReview(reviewDTO);
            return ResponseEntity.ok(reviewResponse);
        } catch (Exception e) {
            logger.error("Error creating review: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewDTO updateReviewDTO) {
        try {
            User user = getCurrentUser();
            ReviewResponse reviewResponse = reviewService.updateReview(reviewId, updateReviewDTO, user.getId());
            return ResponseEntity.ok(reviewResponse);
        } catch (Exception e) {
            logger.error("Error updating review {}: {}", reviewId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) {
        try {
            User user = getCurrentUser();
            reviewService.deleteReview(reviewId, user.getId());
            return ResponseEntity.ok("Đánh giá đã được xóa thành công");
        } catch (Exception e) {
            logger.error("Error deleting review {}: {}", reviewId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReviewById(@PathVariable Long reviewId) {
        try {
            ReviewResponse reviewResponse = reviewService.getReviewById(reviewId);
            return ResponseEntity.ok(reviewResponse);
        } catch (Exception e) {
            logger.error("Error getting review {}: {}", reviewId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getReviewsByProductId(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "false") boolean approvedOnly) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, limit, sort);
            
            Page<ReviewResponse> reviews;
            if (approvedOnly) {
                reviews = reviewService.getApprovedReviewsByProductId(productId, pageable);
            } else {
                reviews = reviewService.getReviewsByProductId(productId, pageable);
            }
            
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            logger.error("Error getting reviews for product {}: {}", productId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getReviewsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, limit, sort);
            
            Page<ReviewResponse> reviews = reviewService.getReviewsByUserId(userId, pageable);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            logger.error("Error getting reviews for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/my-reviews")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            User user = getCurrentUser();
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, limit, sort);
            
            Page<ReviewResponse> reviews = reviewService.getReviewsByUserId(user.getId(), pageable);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            logger.error("Error getting user reviews: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/product/{productId}/summary")
    public ResponseEntity<?> getProductReviewSummary(@PathVariable Long productId) {
        try {
            ProductReviewSummaryResponse summary = reviewService.getProductReviewSummary(productId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("Error getting review summary for product {}: {}", productId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/can-review/{productId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> canUserReviewProduct(@PathVariable Long productId) {
        try {
            User user = getCurrentUser();
            boolean canReview = reviewService.canUserReviewProduct(user.getId(), productId);
            return ResponseEntity.ok(Map.of("canReview", canReview));
        } catch (Exception e) {
            logger.error("Error checking review permission for product {}: {}", productId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // Admin endpoints
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, limit, sort);
            
            Page<ReviewResponse> reviews = reviewService.getPendingReviews(pageable);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            logger.error("Error getting pending reviews: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{reviewId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveReview(
            @PathVariable Long reviewId,
            @RequestBody(required = false) String adminResponse) {
        try {
            ReviewResponse reviewResponse = reviewService.approveReview(reviewId, adminResponse);
            return ResponseEntity.ok(reviewResponse);
        } catch (Exception e) {
            logger.error("Error approving review {}: {}", reviewId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{reviewId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectReview(
            @PathVariable Long reviewId,
            @RequestBody(required = false) String adminResponse) {
        try {
            ReviewResponse reviewResponse = reviewService.rejectReview(reviewId, adminResponse);
            return ResponseEntity.ok(reviewResponse);
        } catch (Exception e) {
            logger.error("Error rejecting review {}: {}", reviewId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}
