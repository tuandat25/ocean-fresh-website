package com.tuandat.oceanfresh_backend.responses;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tuandat.oceanfresh_backend.models.ProductAttributeValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductAttributeValueResponse {
    private Long id;
    
    @JsonProperty("product_id")
    private Long productId;
    
    @JsonProperty("attribute_id")
    private Long attributeId;
    
    @JsonProperty("attribute_name")
    private String attributeName;
    
    private String value;
    
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime updatedAt;
    
    // Static method to convert from entity to response
    public static ProductAttributeValueResponse fromProductAttributeValue(ProductAttributeValue attributeValue) {
        return ProductAttributeValueResponse.builder()
                .id(attributeValue.getId())
                .productId(attributeValue.getProduct().getId())
                .attributeId(attributeValue.getAttribute().getId())
                .attributeName(attributeValue.getAttribute().getName())
                .value(attributeValue.getValue())
                .createdAt(attributeValue.getCreatedAt())
                .updatedAt(attributeValue.getUpdatedAt())
                .build();
    }
}
