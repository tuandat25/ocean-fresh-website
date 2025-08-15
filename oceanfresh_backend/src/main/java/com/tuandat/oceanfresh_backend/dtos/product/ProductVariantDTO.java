package com.tuandat.oceanfresh_backend.dtos.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import com.tuandat.oceanfresh_backend.dtos.attribute.AttributeValueDTO;

import lombok.Data;

@Data
public class ProductVariantDTO {
    private Long id;
    private Long productId;
    private String sku;
    private String variantName;
    private BigDecimal price;
    private BigDecimal oldPrice;
    private int quantityInStock;
    private int soldQuantity;
    private String thumbnailUrl;
    private boolean isActive;
    private Set<AttributeValueDTO> selectedAttributes; // Hiển thị các thuộc tính đã chọn
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}