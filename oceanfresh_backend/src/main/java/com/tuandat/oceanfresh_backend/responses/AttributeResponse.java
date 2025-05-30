package com.tuandat.oceanfresh_backend.responses;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import com.tuandat.oceanfresh_backend.models.Attribute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AttributeResponse extends BaseResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Set<AttributeValueResponse> attributeValues;

    // Static method to convert from entity to response
    public static AttributeResponse fromAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        Set<AttributeValueResponse> valueResponses = Collections.emptySet();
        if (attribute.getAttributeValues() != null && !attribute.getAttributeValues().isEmpty()) {
            valueResponses = attribute.getAttributeValues().stream()
                    .map(AttributeValueResponse::fromAttributeValue)
                    .collect(Collectors.toSet());
        }

        AttributeResponse response = AttributeResponse.builder()
                .id(attribute.getId())
                .name(attribute.getName())
                .code(attribute.getCode())
                .description(attribute.getDescription())
                .attributeValues(valueResponses)
                .build();

        response.setCreatedAt(attribute.getCreatedAt());
        response.setUpdatedAt(attribute.getUpdatedAt());

        return response;
    }
}
