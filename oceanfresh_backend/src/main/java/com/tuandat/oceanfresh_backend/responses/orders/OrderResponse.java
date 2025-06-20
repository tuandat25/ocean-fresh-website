package com.tuandat.oceanfresh_backend.responses.orders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tuandat.oceanfresh_backend.models.Order;
import com.tuandat.oceanfresh_backend.models.PaymentStatus;
import com.tuandat.oceanfresh_backend.responses.BaseResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class OrderResponse extends BaseResponse {
    private Long id;

    @JsonProperty("order_code")
    private String orderCode;

    @JsonProperty("user_id")
    private Long userId;

    private String fullName;

    private String email;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("shipping_address")
    private String shippingAddress;

    private String note;

    @JsonProperty("order_date")
    private LocalDateTime orderDate;

    private String status;

    @JsonProperty("subtotal_amount")
    private BigDecimal subtotalAmount;

    @JsonProperty("shipping_fee")
    private BigDecimal shippingFee;

    @JsonProperty("discount_amount")
    private BigDecimal discountAmount;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @JsonProperty("shipping_method")
    private String shippingMethod;

    @JsonProperty("shipping_date_expected")
    private LocalDate shippingDateExpected;

    @JsonProperty("actual_shipping_date")
    private LocalDateTime actualShippingDate;

    @JsonProperty("tracking_number")
    private String trackingNumber;

    @JsonProperty("payment_method")
    private String paymentMethod;

    @JsonProperty("payment_status")
    private PaymentStatus paymentStatus;

    @JsonProperty("coupon_code")
    private String couponCode;

    @JsonProperty("vnp_txn_ref")
    private String vnpTxnRef;

    @JsonProperty("order_details")
    private List<OrderDetailResponse> orderDetails;

    @JsonProperty("total_items")
    private int totalItems;

    @JsonProperty("total_quantity")
    private int totalQuantity;    

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;




    public static OrderResponse fromOrder(Order order) {
        List<OrderDetailResponse> orderDetailResponses = OrderDetailResponse.fromOrderDetails(order.getOrderDetails());
        
        // Tính tổng số loại sản phẩm (số items khác nhau)
        int totalItems = order.getOrderDetails() != null ? order.getOrderDetails().size() : 0;
        
        // Tính tổng số lượng sản phẩm
        int totalQuantity = order.getOrderDetails() != null ? 
            order.getOrderDetails().stream()
                .mapToInt(detail -> detail.getQuantity())
                .sum() : 0;

        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .fullName(order.getFullName())
                .email(order.getEmail())
                .phoneNumber(order.getPhoneNumber())
                .shippingAddress(order.getShippingAddress())
                .note(order.getNote())
                .orderDate(order.getOrderDate())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .subtotalAmount(order.getSubtotalAmount())
                .shippingFee(order.getShippingFee())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .shippingMethod(order.getShippingMethod())
                .shippingDateExpected(order.getShippingDateExpected())
                .actualShippingDate(order.getActualShippingDate())
                .trackingNumber(order.getTrackingNumber())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .couponCode(order.getCoupon() != null ? order.getCoupon().getCode() : null)
                .vnpTxnRef(order.getVnpTxnRef())
                .orderDetails(orderDetailResponses)
                .totalItems(totalItems)
                .totalQuantity(totalQuantity)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

}
