package com.tuandat.oceanfresh_backend.responses;

import java.util.List;

import com.tuandat.oceanfresh_backend.models.Category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
                .build();
        return categoryResponse;
    }
}