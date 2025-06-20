package com.tuandat.oceanfresh_backend.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    @NotNull(message = "Mã sản phẩm không được để trống")
    @JsonProperty("product_variant_id")
    private Long productVariantId;
    
    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải ít nhất là 1")
    @Max(value = 999, message = "Số lượng không được vượt quá 999")
    private Integer quantity;
    
    // Không cần unitPrice - Server sẽ lấy giá trực tiếp từ database
    // Đảm bảo tính bảo mật và tính toàn vẹn dữ liệu
}