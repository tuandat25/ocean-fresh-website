package com.tuandat.oceanfresh_backend.responses.attribute;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.tuandat.oceanfresh_backend.models.Attribute;
import com.tuandat.oceanfresh_backend.responses.AttributeValueResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeWithValuesResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AttributeValueResponse> values;

    public static AttributeWithValuesResponse fromAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        List<AttributeValueResponse> valueResponses = null;
        if (attribute.getAttributeValues() != null) {
            valueResponses = attribute.getAttributeValues().stream()
                    .map(AttributeValueResponse::fromAttributeValue)
                    .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
                    .collect(Collectors.toList());
        }

        return AttributeWithValuesResponse.builder()
                .id(attribute.getId())
                .name(attribute.getName())
                .code(attribute.getCode())
                .description(attribute.getDescription())
                .createdAt(attribute.getCreatedAt())
                .updatedAt(attribute.getUpdatedAt())
                .values(valueResponses)
                .build();
    }
}
