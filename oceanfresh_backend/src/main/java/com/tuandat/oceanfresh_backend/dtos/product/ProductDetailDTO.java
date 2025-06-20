package com.tuandat.oceanfresh_backend.dtos.product;


import java.util.List;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductDetailDTO extends ProductDTO {
    private String description;
    private String brand;
    private String origin;
    private Set<ProductVariantDTO> variants;
    private List<ProductImageDTO> images; // Thêm trường để chứa danh sách ảnh
    // Thêm các trường thuộc tính chung của sản phẩm nếu cần
}