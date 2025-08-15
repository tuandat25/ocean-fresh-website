package com.tuandat.oceanfresh_backend.dtos.attribute;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeWithValuesDTO {

    @NotBlank(message = "Tên thuộc tính không được để trống")
    @Size(min = 2, max = 100, message = "Tên thuộc tính phải từ 2 đến 100 ký tự")
    private String name;

    @NotBlank(message = "Mã thuộc tính không được để trống")
    @Size(min = 2, max = 50, message = "Mã thuộc tính phải từ 2 đến 50 ký tự")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Mã thuộc tính chỉ được chứa chữ hoa, số và dấu gạch dưới (_)")
    private String code;

    private String description;

    @NotEmpty(message = "Thuộc tính phải có ít nhất 1 giá trị")
    @Valid
    private List<AttributeValueCreateDTO> values;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttributeValueCreateDTO {
        @NotBlank(message = "Giá trị thuộc tính không được để trống")
        @Size(max = 255, message = "Giá trị thuộc tính phải nhỏ hơn 255 ký tự")
        private String value;

        private Integer displayOrder = 0; // Default to 0 if not provided
    }
}
