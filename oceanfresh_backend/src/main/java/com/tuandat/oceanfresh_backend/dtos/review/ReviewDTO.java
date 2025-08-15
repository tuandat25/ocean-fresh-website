package com.tuandat.oceanfresh_backend.dtos.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO {
    
    @NotNull(message = "ID sản phẩm không được để trống")
    private Long productId;
    
    @NotNull(message = "ID người dùng không được để trống")
    private Long userId;
    
    private Long orderDetailId; // Optional - để verify người dùng đã mua
    
    @NotNull(message = "Đánh giá không được để trống")
    @Min(value = 1, message = "Đánh giá phải từ 1 đến 5 sao")
    @Max(value = 5, message = "Đánh giá phải từ 1 đến 5 sao")
    private Integer rating;
    
    @Size(max = 255, message = "Tiêu đề không được quá 255 ký tự")
    private String title;
    
    @Size(max = 1000, message = "Nội dung đánh giá không được quá 1000 ký tự")
    private String content;
}
