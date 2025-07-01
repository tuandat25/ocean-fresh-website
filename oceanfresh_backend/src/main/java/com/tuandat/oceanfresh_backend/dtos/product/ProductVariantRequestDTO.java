package com.tuandat.oceanfresh_backend.dtos.product;

import java.math.BigDecimal;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductVariantRequestDTO {
    // Không cần productId ở đây nếu endpoint là /products/{productId}/variants

    // SKU không bắt buộc khi thêm mới (sẽ được tự động sinh), có thể sửa khi update
    @Size(max = 100, message = "SKU không được vượt quá 100 ký tự")
    private String sku;

    @JsonProperty("variant_name")
    private String variantName;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Giá phải là số không âm")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = true, message = "Giá cũ phải là số không âm")
    @JsonProperty("old_price")
    private BigDecimal oldPrice;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng không được âm")
    @JsonProperty("quantity_in_stock")
    private int quantityInStock;

    private String thumbnailUrl;
    private boolean isActive = true;

    @NotNull(message = "Danh sách thuộc tính không được để trống")
    @NotEmpty(message = "Phải chọn ít nhất một giá trị thuộc tính để định nghĩa một biến thể")
    @JsonProperty("attribute_value_ids")
    private Set<Long> selectedAttributeValueIds; // Danh sách ID của các AttributeValue
}
