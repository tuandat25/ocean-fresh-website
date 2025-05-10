package com.tuandat.oceanfresh_backend.responses.product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tuandat.oceanfresh_backend.models.Product;
import com.tuandat.oceanfresh_backend.models.ProductImage;
import com.tuandat.oceanfresh_backend.responses.BaseResponse;
import com.tuandat.oceanfresh_backend.responses.ProductAttributeValueResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper=false)
public class ProductResponse extends BaseResponse {
    private Long id;
    private String name;
    private Float price;
    private String thumbnail;
    private Long quantity;
    private Long soldQuantity;
    private String description;
    // Thêm trường totalPages
    private int totalPages;    

    @JsonProperty("product_images")
    @Builder.Default
    private List<ProductImage> productImages = new ArrayList<>();

    @JsonProperty("attribute_values")
    @Builder.Default
    private List<ProductAttributeValueResponse> attributeValues = new ArrayList<>();
    
    @JsonProperty("grouped_attributes")
    @Builder.Default
    private Map<String, List<String>> groupedAttributes = new HashMap<>();

    @JsonProperty("category_id")
    private Long categoryId;    
    public static ProductResponse fromProduct(Product product) {
        // Tạo map để nhóm thuộc tính theo tên
        Map<String, List<String>> groupedAttrs = new HashMap<>();
        
        if (product.getAttributeValues() != null) {
            // Nhóm các thuộc tính theo tên
            product.getAttributeValues().forEach(attrValue -> {
                String attributeName = attrValue.getAttribute().getName();
                String value = attrValue.getValue();
                
                // Tạo danh sách nếu chưa tồn tại
                if (!groupedAttrs.containsKey(attributeName)) {
                    groupedAttrs.put(attributeName, new ArrayList<>());
                }
                
                // Thêm giá trị vào danh sách thuộc tính
                groupedAttrs.get(attributeName).add(value);
            });
        }
        
        ProductResponse productResponse = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .thumbnail(product.getThumbnail())
                .description(product.getDescription())
                .categoryId(product.getCategory().getId())
                .quantity(product.getQuantity())
                .soldQuantity(product.getSoldQuantity())
                .productImages(product.getProductImages())
                .attributeValues(
                    product.getAttributeValues() != null ?
                    product.getAttributeValues().stream()
                        .map(ProductAttributeValueResponse::fromProductAttributeValue)
                        .collect(Collectors.toList()) : 
                    new ArrayList<>()
                )
                .groupedAttributes(groupedAttrs)
                .build();
        productResponse.setCreatedAt(product.getCreatedAt());
        productResponse.setUpdatedAt(product.getUpdatedAt());
        return productResponse;
    }
}
