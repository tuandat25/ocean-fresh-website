package com.tuandat.oceanfresh_backend.dtos.product;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductUpdateDTO { // Đổi tên để rõ ràng hơn
    // Các trường không bắt buộc @NotBlank, vì client có thể chỉ muốn cập nhật một vài thông tin
    @Size(max = 255, message = "Product name must be less than 255 characters")
    private String name;

    @Size(max = 280, message = "Slug must be less than 280 characters")
    private String slug;

    private String description;

    @JsonProperty("category_id")
    private Long categoryId; // Cho phép thay đổi category
    private String brand;
    private String origin;

    @JsonProperty("storage_instruction")
    private String storageInstruction;
    @JsonProperty("harvest_date")
    private LocalDate harvestDate; // Giữ nguyên kiểu String để dễ dàng nhận dữ liệu từ

    @JsonProperty("freshness_guarantee_period")
    private String freshnessGuaranteePeriod;

    @JsonProperty("harvest_area")
    private String harvestArea;
    @JsonProperty("return_policy")
    private String returnPolicy;
    

    @JsonProperty("is_active")
    private boolean isActive; // Client có thể muốn cập nhật trạng thái active/inactive
}
