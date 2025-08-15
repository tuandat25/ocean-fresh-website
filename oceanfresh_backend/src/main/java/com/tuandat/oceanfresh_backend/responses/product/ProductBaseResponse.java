package com.tuandat.oceanfresh_backend.responses.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tuandat.oceanfresh_backend.models.Product;
import com.tuandat.oceanfresh_backend.models.ProductImage;
import com.tuandat.oceanfresh_backend.responses.CategoryResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductBaseResponse { // Đổi tên để rõ ràng hơn
    private Long id;
    private String name;
    private String slug;
    private String mainImageUrl;

    @JsonProperty("isActive")
    private boolean isActive;
    private CategoryResponse category; // Dùng CategoryResponse
    private LocalDateTime createdAt;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String categoryName; // Tên danh mục để hiển thị
    private Long variantCount; // Số lượng biến thể của sản phẩm
    // Thêm các trường khác nếu cần
    // Ví dụ: mô tả, số lượng tồn kho, v.v.
    private String description;

    @JsonProperty("product_images")
    private List<ProductImage> productImages = new ArrayList<>();


    public static ProductBaseResponse fromEntity(Product product) {
        if (product == null) {
            return null;
        }

        BigDecimal min = null;
        BigDecimal max = null;

        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            min = product.getVariants().stream()
                    .filter(v -> v.isActive() && v.getPrice() != null)
                    .map(com.tuandat.oceanfresh_backend.models.ProductVariant::getPrice)
                    .min(Comparator.naturalOrder())
                    .orElse(null);  
            max = product.getVariants().stream()
                    .filter(v -> v.isActive() && v.getPrice() != null)
                    .map(com.tuandat.oceanfresh_backend.models.ProductVariant::getPrice)
                    .max(Comparator.naturalOrder())
                    .orElse(null);
        }

        return ProductBaseResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .mainImageUrl(product.getMainImageUrl())
                .isActive(product.isActive())
                .category(product.getCategory() != null ? CategoryResponse.fromCategory(product.getCategory()) : null)
                .createdAt(product.getCreatedAt())
                .minPrice(min)
                .maxPrice(max)
                .description(product.getDescription())
                .productImages(product.getProductImages())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .variantCount(product.getVariants() != null ? (long) product.getVariants().size() : 0L)
                .build();
    }
}
