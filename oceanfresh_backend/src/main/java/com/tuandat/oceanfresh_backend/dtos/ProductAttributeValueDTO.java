package com.tuandat.oceanfresh_backend.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductAttributeValueDTO {
    
    private Long id;
    
    @NotNull(message = "ID sản phẩm không được để trống")
    @JsonProperty("product_id")
    private Long productId;
    
    @NotNull(message = "ID thuộc tính không được để trống")
    @JsonProperty("attribute_id")
    private Long attributeId;
    
    @NotBlank(message = "Giá trị thuộc tính không được để trống")
    private String value;
    
    // Các trường bổ sung không bắt buộc khi tạo/cập nhật
    @JsonProperty("attribute_name")
    private String attributeName;
}
