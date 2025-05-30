package com.tuandat.oceanfresh_backend.dtos.product;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateDTO { // Đổi tên để rõ ràng hơn
    @NotBlank(message = "Product name cannot be blank")
    @Size(max = 255, message = "Product name must be less than 255 characters")
    private String name;

    // Slug có thể được tạo tự động ở backend nếu không cung cấp
    @Size(max = 280, message = "Slug must be less than 280 characters")
    private String slug;

    private String description;

    @NotNull(message = "Category ID cannot be null for a new product")
    @JsonProperty("category_id")
    private Long categoryId; // Bắt buộc khi tạo mới

    private String brand;
    private String origin;

    @JsonProperty("main_image_url")
    private String mainImageUrl;

    @JsonProperty("is_active")
    private Boolean isActive = true; // Mặc định là active
}
