package com.tuandat.oceanfresh_backend.dtos.attribute;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeValueCreateDTO {
    @NotBlank(message = "Giá trị thuộc tính không được để trống")
    @Size(max = 255, message = "Giá trị thuộc tính phải nhỏ hơn 255 ký tự")
    private String value;

    @JsonProperty("display_order")
    private Integer displayOrder = 0; // Default to 0 if not provided
}