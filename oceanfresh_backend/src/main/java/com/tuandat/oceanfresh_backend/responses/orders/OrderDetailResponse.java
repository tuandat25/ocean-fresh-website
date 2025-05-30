package com.tuandat.oceanfresh_backend.responses.orders;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tuandat.oceanfresh_backend.responses.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import com.tuandat.oceanfresh_backend.models.OrderDetail;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class OrderDetailResponse extends BaseResponse {
    private Long id;

    @JsonProperty("product_variant_id")
    private Long productVariantId;

    @JsonProperty("product_variant_sku")
    private String productVariantSku;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("variant_name")
    private String variantName;

    private int quantity;

    @JsonProperty("price_at_order")
    private BigDecimal priceAtOrder;

    @JsonProperty("total_line_amount")
    private BigDecimal totalLineAmount;

    /**
     * Convert từ OrderDetail entity sang OrderDetailResponse
     * @param orderDetail OrderDetail entity
     * @return OrderDetailResponse
     */
    public static OrderDetailResponse fromOrderDetail(OrderDetail orderDetail) {
        if (orderDetail == null) {
            return null;
        }

        String productName = null;
        String variantName = null;
        String productVariantSku = null;

        // Lấy thông tin từ ProductVariant
        if (orderDetail.getProductVariant() != null) {
            productVariantSku = orderDetail.getProductVariant().getSku();
            variantName = orderDetail.getProductVariant().getVariantName();
            
            // Lấy tên sản phẩm từ Product
            if (orderDetail.getProductVariant().getProduct() != null) {
                productName = orderDetail.getProductVariant().getProduct().getName();
            }
        }

        return OrderDetailResponse.builder()
                .id(orderDetail.getId())
                .productVariantId(orderDetail.getProductVariant() != null ? 
                    orderDetail.getProductVariant().getId() : null)
                .productVariantSku(productVariantSku)
                .productName(productName)
                .variantName(variantName)
                .quantity(orderDetail.getQuantity())
                .priceAtOrder(orderDetail.getPriceAtOrder())
                .totalLineAmount(orderDetail.getTotalLineAmount())
                .build();
    }

    /**
     * Convert từ List<OrderDetail> sang List<OrderDetailResponse>
     * @param orderDetails List OrderDetail entities
     * @return List<OrderDetailResponse>
     */
    public static List<OrderDetailResponse> fromOrderDetails(List<OrderDetail> orderDetails) {
        if (orderDetails == null || orderDetails.isEmpty()) {
            return List.of(); // Trả về empty list thay vì null
        }

        return orderDetails.stream()
                .map(OrderDetailResponse::fromOrderDetail)
                .collect(Collectors.toList());
    }
}
