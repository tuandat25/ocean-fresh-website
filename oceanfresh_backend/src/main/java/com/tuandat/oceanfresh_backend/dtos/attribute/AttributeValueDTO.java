package com.tuandat.oceanfresh_backend.dtos.attribute;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeValueDTO {

    @NotNull(message = "ID thuộc tính không được để trống")
    private Long attributeId; // ID of the parent Attribute

    private String attributeName; // Name of the parent Attribute

    @NotBlank(message = "Giá trị thuộc tính không được để trống")
    @Size(max = 255, message = "Giá trị thuộc tính phải nhỏ hơn 255 ký tự")
    private String value;

    private Integer displayOrder = 0; // Default to 0 if not provided
}
