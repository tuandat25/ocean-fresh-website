package com.tuandat.oceanfresh_backend.responses;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tuandat.oceanfresh_backend.models.Category;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryResponse extends BaseResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    
    private String message;
    private List<String> errors;
    private Category category;
    private int totalPages;

    public static CategoryResponse fromCategory(Category category) {
        CategoryResponse categoryResponse = CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .build();
        categoryResponse.setCreatedAt(category.getCreatedAt());
        categoryResponse.setUpdatedAt(category.getUpdatedAt());
        return categoryResponse;
    }
}