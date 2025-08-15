package com.tuandat.oceanfresh_backend.dtos;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdateDTO {
    @JsonProperty("shipping_date_expected")
    @Future(message = "Ngày giao hàng phải trong tương lai")
    private LocalDate shippingDateExpected;

    @JsonProperty("status")
    private String status;

    @JsonProperty("shipping_method")
    private String shippingMethod; // STANDARD (Tiêu chuẩn), EXPRESS (Nhanh), v.v.

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    @Size(max = 250, message = "Địa chỉ giao hàng không được vượt quá 250 ký tự")
    @JsonProperty("shipping_address")
    private String shippingAddress;

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String note;
}
