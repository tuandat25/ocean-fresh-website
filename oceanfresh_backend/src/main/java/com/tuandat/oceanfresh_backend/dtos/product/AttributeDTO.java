package com.tuandat.oceanfresh_backend.dtos.product;

import java.time.LocalDateTime;

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
    @Size(max = 100, message = "Tên thuộc tính phải nhỏ hơn 100 ký tự")
    private String name;

    @NotBlank(message = "Mã thuộc tính không được để trống")
    @Size(max = 50, message = "Mã thuộc tính phải nhỏ hơn 50 ký tự")
    private String code;

    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    
}
