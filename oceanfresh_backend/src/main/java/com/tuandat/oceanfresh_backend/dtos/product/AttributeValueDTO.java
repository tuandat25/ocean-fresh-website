package com.tuandat.oceanfresh_backend.dtos.product;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeValueDTO {
    private Long id;

    @NotNull(message = "ID của Attribute cha không được để trống")
    private Long attributeId; // ID của Attribute cha

    private String attributeName; // Tên của Attribute cha (để hiển thị)

    @NotBlank(message = "Giá trị thuộc tính không được để trống")
    private String value;

    private int displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}