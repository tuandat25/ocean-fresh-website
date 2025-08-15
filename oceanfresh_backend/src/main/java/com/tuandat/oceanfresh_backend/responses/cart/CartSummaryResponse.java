package com.tuandat.oceanfresh_backend.responses.cart;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartSummaryResponse {

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
}
