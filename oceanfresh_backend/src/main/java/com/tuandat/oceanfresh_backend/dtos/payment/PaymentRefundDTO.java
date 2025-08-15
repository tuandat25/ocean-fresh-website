package com.tuandat.oceanfresh_backend.dtos.payment;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PaymentRefundDTO {

    @JsonProperty("transaction_type")
    @NotBlank(message = "Loại giao dịch không được để trống")
    @Pattern(regexp = "^(02|03)$", message = "Loại giao dịch phải là 02 (hoàn tiền toàn bộ) hoặc 03 (hoàn tiền một phần)")
    private String transactionType;

    @JsonProperty("order_id")
    @NotBlank(message = "Mã đơn hàng không được để trống")
    @Size(min = 1, max = 50, message = "Mã đơn hàng phải từ 1-50 ký tự")
    private String orderId;

    @JsonProperty("amount")
    @NotNull(message = "Số tiền hoàn trả không được để trống")
    @Min(value = 1000, message = "Số tiền hoàn trả tối thiểu là 1,000 VND")
    @Max(value = 999999999, message = "Số tiền hoàn trả tối đa là 999,999,999 VND")
    private Long amount;

    @JsonProperty("transaction_date")
    @NotBlank(message = "Ngày giao dịch không được để trống")
    @Pattern(regexp = "^\\d{14}$", message = "Ngày giao dịch phải có định dạng yyyyMMddHHmmss")
    private String transactionDate;

    @JsonProperty("created_by")
    @NotBlank(message = "Người thực hiện không được để trống")
    @Size(min = 1, max = 50, message = "Người thực hiện phải từ 1-50 ký tự")
    private String createdBy;

    @JsonProperty("ip_address")
    @NotBlank(message = "Địa chỉ IP không được để trống")
    @Pattern(regexp = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$", message = "Địa chỉ IP không hợp lệ")
    private String ipAddress;

    // Thêm các field bổ sung cho VNPay
    @JsonProperty("vnp_txn_ref")
    private String vnpTxnRef; // Mã giao dịch gốc từ VNPay
    
    @JsonProperty("original_transaction_date")
    private String originalTransactionDate; // Ngày giao dịch gốc
}