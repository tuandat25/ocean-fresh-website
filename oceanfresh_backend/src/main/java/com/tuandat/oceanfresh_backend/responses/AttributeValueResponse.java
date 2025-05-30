package com.tuandat.oceanfresh_backend.responses;

import com.tuandat.oceanfresh_backend.models.Attribute;
import com.tuandat.oceanfresh_backend.models.AttributeValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false) // To suppress warning if BaseResponse is simple or no specific equals/hash needed for it here
public class AttributeValueResponse extends BaseResponse {
    private Long id;
    private Long attributeId;
    private String attributeName;
    private String attributeCode;
    private String value;
    private int displayOrder;

    public static AttributeValueResponse fromAttributeValue(AttributeValue attributeValue) {
        if (attributeValue == null) {
            return null;
        }

        Long attrId = null;
        String attrName = null;
        String attrCode = null;

        Attribute attribute = attributeValue.getAttribute();
        if (attribute != null) {
            attrId = attribute.getId();
            attrName = attribute.getName();
            attrCode = attribute.getCode();
        }

        AttributeValueResponse response = AttributeValueResponse.builder()
                .id(attributeValue.getId())
                .attributeId(attrId)
                .attributeName(attrName)
                .attributeCode(attrCode)
                .value(attributeValue.getValue())
                .displayOrder(attributeValue.getDisplayOrder())
                .build();

        // Assuming BaseResponse has createdAt and updatedAt setters
        // and AttributeValue entity has these fields (e.g., from a BaseEntity)
        if (attributeValue.getCreatedAt() != null) {
            response.setCreatedAt(attributeValue.getCreatedAt());
        }
        if (attributeValue.getUpdatedAt() != null) {
            response.setUpdatedAt(attributeValue.getUpdatedAt());
        }

        return response;
    }
}
