package com.tuandat.oceanfresh_backend.responses;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse extends BaseResponse {
    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("fullname")  
    private String fullName;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private String address;

    @JsonProperty("order_date")
    private LocalDateTime orderDate;

    private String status;

    @JsonProperty("total_money")
    private Float totalMoney;

    @JsonProperty("shipping_method")
    private String shippingMethod;
    
    @JsonProperty("shipping_address")
    private String shippingAddress;

    @JsonProperty("shipping_date")
    private LocalDate shippingDate;

    @JsonProperty("payment_method")
    private String paymentMethod;

    @JsonProperty("active")
    private Boolean active;
}
