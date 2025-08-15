package com.tuandat.oceanfresh_backend.dtos.product;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductDTO {
    private Long id;

    @NotBlank(message = "Product name cannot be blank")
    @Size(max = 255, message = "Product name must be less than 255 characters")
    private String name;

    private String slug; // Nên tự động tạo từ name nếu không cung cấp

    private String mainImageUrl;
    private boolean isActive;
    private Long categoryId;
    private String categoryName; // Tên danh mục để hiển thị
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // Có thể thêm giá min/max của các variant nếu cần hiển thị ở list
}

