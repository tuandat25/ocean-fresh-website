package com.tuandat.oceanfresh_backend.dtos.product;


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
    // Thêm các trường thuộc tính chung của sản phẩm nếu cần
}