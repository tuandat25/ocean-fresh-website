package com.tuandat.oceanfresh_backend.dtos.attribute;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

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
public class AttributeCreateDTO {
    @NotBlank(message = "Tên thuộc tính không được để trống")
    @Size(min = 2, max = 100, message = "Tên thuộc tính phải từ 2 đến 100 ký tự")
    @JsonProperty("attribute_name")
    private String attributeName;

    @NotBlank(message = "Mã thuộc tính không được để trống")
    @Size(min = 2, max = 50, message = "Mã thuộc tính phải từ 2 đến 50 ký tự")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Mã thuộc tính chỉ được chứa chữ hoa, số và dấu gạch dưới (_)")
    @JsonProperty("attribute_code")
    private String attributeCode;
    
    @JsonProperty("attribute_description")
    private String attributeDescription;

    @NotEmpty(message = "Thuộc tính phải có ít nhất 1 giá trị")
    @Valid
    @JsonProperty("values")
    private List<AttributeValueCreateDTO> values;

}
