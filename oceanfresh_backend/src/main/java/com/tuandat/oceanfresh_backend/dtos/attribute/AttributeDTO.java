package com.tuandat.oceanfresh_backend.dtos.attribute;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeDTO {

    @NotBlank(message = "Tên thuộc tính không được để trống")
    @Size(min = 2, max = 100, message = "Tên thuộc tính phải từ 2 đến 100 ký tự")
    private String name;

    @NotBlank(message = "Mã thuộc tính không được để trống")
    @Size(min = 2, max = 50, message = "Mã thuộc tính phải từ 2 đến 50 ký tự")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Mã thuộc tính chỉ được chứa chữ hoa, số và dấu gạch dưới (_)")
    private String code;

    private String description;
}
