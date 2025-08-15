package com.tuandat.oceanfresh_backend.responses.cart;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    @JsonProperty("cart_id")
    private Long cartId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("cart_items")
    private List<CartItemResponse> cartItems;

    @JsonProperty("total_items")
    private Integer totalItems;

    @JsonProperty("total_quantity")
    private Integer totalQuantity;

    @JsonProperty("subtotal_amount")
    private BigDecimal subtotalAmount;

    @JsonProperty("estimated_shipping")
    private BigDecimal estimatedShipping;

    @JsonProperty("estimated_total")
    private BigDecimal estimatedTotal;

    @JsonProperty("is_empty")
    private Boolean isEmpty;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;
}
