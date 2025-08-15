package com.tuandat.oceanfresh_backend.dtos.cart;

import com.fasterxml.jackson.annotation.JsonProperty;

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
public class AddToCartDTO {

    @NotNull(message = "Product variant ID không được để trống")
    @JsonProperty("product_variant_id")
    private Long productVariantId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("session_id")
    private String sessionId;
}
