package com.tuandat.oceanfresh_backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttributeDTO {
    
    private Long id;
    
    @NotBlank(message = "Tên thuộc tính không được để trống")
    @Size(min = 2, max = 100, message = "Tên thuộc tính phải từ 2-100 ký tự")
    private String name;
}
