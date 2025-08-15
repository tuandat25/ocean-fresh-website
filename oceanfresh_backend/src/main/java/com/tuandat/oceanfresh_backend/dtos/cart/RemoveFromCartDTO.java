package com.tuandat.oceanfresh_backend.dtos.cart;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoveFromCartDTO {

    @NotNull(message = "Product variant ID không được để trống")
    @JsonProperty("product_variant_id")
    private Long productVariantId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("session_id")
    private String sessionId;
}
