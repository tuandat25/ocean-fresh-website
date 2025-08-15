package com.tuandat.oceanfresh_backend.responses.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductReviewSummaryResponse {
    private Long productId;
    private String productName;
    private Double averageRating;
    private Long totalReviews;
    private Map<Integer, Long> ratingDistribution; // Rating (1-5) -> Count
}
