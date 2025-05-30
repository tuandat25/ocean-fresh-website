package com.tuandat.oceanfresh_backend.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
    
    // Giá sẽ được kiểm tra với database, không cần người dùng nhập
    // Nhưng vẫn nhận để kiểm tra lại
    @JsonProperty("unit_price")
    @DecimalMin(value = "0.0", inclusive = false, message = "Đơn giá phải lớn hơn 0")
    private BigDecimal unitPrice;
}