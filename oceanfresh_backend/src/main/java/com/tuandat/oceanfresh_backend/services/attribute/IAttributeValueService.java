package com.tuandat.oceanfresh_backend.services.attribute;

import java.util.List;

import com.tuandat.oceanfresh_backend.dtos.attribute.AttributeValueDTO;
import com.tuandat.oceanfresh_backend.responses.AttributeValueResponse;

public interface IAttributeValueService {
    // Tạo mới một giá trị thuộc tính
    AttributeValueResponse createAttributeValue(AttributeValueDTO attributeValueDTO);

    // Lấy giá trị thuộc tính theo ID
    AttributeValueResponse getAttributeValueById(Long valueId);

    // Lấy tất cả giá trị thuộc tính thuộc về một thuộc tính
    List<AttributeValueResponse> getAttributeValuesByAttributeId(Long attributeId);

    // Cập nhật giá trị thuộc tính
    AttributeValueResponse updateAttributeValue(Long valueId, AttributeValueDTO attributeValueDTO);

    // Xóa giá trị thuộc tính
    void deleteAttributeValue(Long valueId);
    
    // Kiểm tra giá trị đã tồn tại cho thuộc tính cụ thể chưa
    boolean existsByAttributeIdAndValue(Long attributeId, String value);
}
