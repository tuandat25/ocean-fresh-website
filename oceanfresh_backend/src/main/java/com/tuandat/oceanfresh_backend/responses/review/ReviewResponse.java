package com.tuandat.oceanfresh_backend.responses.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Long orderDetailId;
    private Integer rating;
    private String title;
    private String content;
    private Boolean isApproved;
    private String adminResponses;
    private String createdAt;
    private String updatedAt;
    
    // Thông tin thêm cho người dùng
    private Boolean canEdit; // User có thể edit review này không
    private Boolean isVerifiedPurchase; // Đã mua sản phẩm hay chưa
}
