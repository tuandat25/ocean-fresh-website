package com.tuandat.oceanfresh_backend.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data//toString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDTO {
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Name must be between 3 and 200 characters")
    private String name;

    @Min(value = 0, message = "Price must be greater than or equal to 0")
    @Max(value = 10000000, message = "Price must be less than or equal to 10,000,000")
    private Float price;

    private String thumbnail;

    private String description;

    @JsonProperty("category_id")
    private Long categoryId;

    @Min(value = 0, message = "Quantity must be greater than or equal to 0")
    @Max(value = 10000, message = "Quantity must be less than or equal to 10,000")
    @JsonProperty("quantity")
    private Long quantity;

    @JsonProperty("sold_quantity")
    @Min(value = 0, message = "Sold quantity must be greater than or equal to 0")
    @Max(value = 10000, message = "Sold quantity must be less than or equal to 10,000")
    @Builder.Default
    private Long soldQuantity = 0L;
}

