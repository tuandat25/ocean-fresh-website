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
public class CartItemResponse {

    @JsonProperty("cart_item_id")
    private Long cartItemId;

    @JsonProperty("product_variant_id")
    private Long productVariantId;

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("product_slug")
    private String productSlug;

    @JsonProperty("variant_name")
    private String variantName;

    @JsonProperty("variant_sku")
    private String variantSku;

    @JsonProperty("variant_image")
    private String variantImage;

    @JsonProperty("main_image_url")
    private String mainImageUrl;

    @JsonProperty("unit_price")
    private BigDecimal unitPrice;

    @JsonProperty("price_at_addition")
    private BigDecimal priceAtAddition;

    private Integer quantity;

    @JsonProperty("line_total")
    private BigDecimal lineTotal;

    @JsonProperty("stock_quantity")
    private Integer stockQuantity;

    @JsonProperty("is_available")
    private Boolean isAvailable;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;
}
