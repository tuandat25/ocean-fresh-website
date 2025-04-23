package com.tuandat.oceanfresh_backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryCreateDTO {
    
    @NotBlank(message = "Category name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Image file is required")
    private MultipartFile file;
}