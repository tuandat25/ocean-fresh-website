package com.tuandat.oceanfresh_backend.dtos.product;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateDTO { // Đổi tên để rõ ràng hơn
    // Các trường không bắt buộc @NotBlank, vì client có thể chỉ muốn cập nhật một vài thông tin
    @Size(max = 255, message = "Product name must be less than 255 characters")
    private String name;

    @Size(max = 280, message = "Slug must be less than 280 characters")
    private String slug;

    private String description;
    private Long categoryId; // Cho phép thay đổi category
    private String brand;
    private String origin;
    private String mainImageUrl;
    private Boolean isActive; // Client có thể muốn cập nhật trạng thái active/inactive
}
