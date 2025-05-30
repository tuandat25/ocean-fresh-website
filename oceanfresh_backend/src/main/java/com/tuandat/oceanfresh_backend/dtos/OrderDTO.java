package com.tuandat.oceanfresh_backend.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    @JsonProperty("user_id")
    private Long userId; // Có thể null cho khách hàng chưa đăng ký
    
    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    private String fullname;
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Định dạng email không hợp lệ")
    @Size(max = 150, message = "Email không được vượt quá 150 ký tự")
    private String email;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 chữ số")
    @JsonProperty("phone_number")
    private String phoneNumber;
    
    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    @Size(max = 250, message = "Địa chỉ giao hàng không được vượt quá 250 ký tự")
    @JsonProperty("shipping_address")
    private String shippingAddress;
    
    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String note;
    
    @JsonProperty("shipping_date_expected")
    @Future(message = "Ngày giao hàng phải trong tương lai")
    private LocalDate shippingDateExpected;
    
    @NotBlank(message = "Phương thức thanh toán không được để trống")
    @JsonProperty("payment_method")
    private String paymentMethod; // COD, VNPAY, MOMO, v.v.
    
    @JsonProperty("shipping_method")
    private String shippingMethod; // STANDARD (Tiêu chuẩn), EXPRESS (Nhanh), v.v.
    
    @JsonProperty("coupon_code")
    private String couponCode;
    
    @JsonProperty("vnp_txn_ref")
    private String vnpTxnRef; // Mã giao dịch VNPay
    
    @NotEmpty(message = "Đơn hàng phải có ít nhất một sản phẩm")
    @Valid
    @JsonProperty("order_items")
    private List<OrderItemDTO> orderItems;
}