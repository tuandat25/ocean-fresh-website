package com.tuandat.oceanfresh_backend.dtos.product;

import java.time.LocalDate;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateDTO { // Đổi tên để rõ ràng hơn
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm phải dưới 255 ký tự")
    private String name;

    // Slug có thể được tạo tự động ở backend nếu không cung cấp
    @Size(max = 280, message = "Slug phải dưới 280 ký tự")
    private String slug;

    private String description;

    @NotNull(message = "Mã danh mục là bắt buộc")
    @JsonProperty("category_id")
    private Long categoryId; // Bắt buộc khi tạo mới

    private String brand;
    private String origin;

    @JsonProperty("main_image_url")
    private String mainImageUrl;

    @JsonProperty("is_active")
    private Boolean isActive = true; // Mặc định là active
    
    // Các trường mới được thêm vào
    @JsonProperty("storage_instruction")
    @Size(max = 255, message = "Mô tả hướng dẫn bảo quản phải dưới 255 ký tự")
    private String storageInstruction;

    @JsonProperty("harvest_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate harvestDate;

    @JsonProperty("freshness_guarantee_period")
    @Size(max = 50, message = "Mô tả thời gian đảm bảo độ tươi ngon phải dưới 50 ký tự")
    private String freshnessGuaranteePeriod;

    @JsonProperty("harvest_area")
    @Size(max = 255, message = "Khu vực giao hàng phải dưới 255 ký tự")
    private String harvestArea;

    @JsonProperty("return_policy")
    @Size(max = 255, message = "Chính sách trả hàng phải dưới 255 ký tự")
    private String returnPolicy;

    // Bắt buộc phải có ít nhất 1 biến thể khi tạo sản phẩm
    @NotEmpty(message = "Sản phẩm phải có ít nhất 1 biến thể")
    @Valid
    private Set<ProductVariantRequestDTO> variants;
}
