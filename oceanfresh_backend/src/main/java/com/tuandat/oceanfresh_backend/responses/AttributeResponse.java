package com.tuandat.oceanfresh_backend.responses;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tuandat.oceanfresh_backend.models.Attribute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttributeResponse extends BaseResponse {
    private Long id;
    private String name;
    
    // Static method to convert from entity to response
    public static AttributeResponse fromAttribute(Attribute attribute) {
        AttributeResponse response = AttributeResponse.builder()
                .id(attribute.getId())
                .name(attribute.getName())
                .build();
                response.setCreatedAt(attribute.getCreatedAt());
                response.setUpdatedAt(attribute.getUpdatedAt());
        return response;
    }
}
