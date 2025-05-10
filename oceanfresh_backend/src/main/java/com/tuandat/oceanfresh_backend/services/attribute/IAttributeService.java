package com.tuandat.oceanfresh_backend.services.attribute;

import java.util.List;

import com.tuandat.oceanfresh_backend.dtos.AttributeDTO;
import com.tuandat.oceanfresh_backend.exceptions.DataNotFoundException;
import com.tuandat.oceanfresh_backend.models.Attribute;
import com.tuandat.oceanfresh_backend.responses.AttributeResponse;

public interface IAttributeService {
    // Tạo mới một thuộc tính
    Attribute createAttribute(AttributeDTO attributeDTO);
    
    // Lấy thuộc tính theo ID
    Attribute getAttributeById(Long id) throws DataNotFoundException;
    
    // Lấy tất cả thuộc tính
    List<AttributeResponse> getAllAttributes();
    
    // Cập nhật thuộc tính
    Attribute updateAttribute(Long id, AttributeDTO attributeDTO) throws DataNotFoundException;
    
    // Xóa thuộc tính
    void deleteAttribute(Long id) throws DataNotFoundException;
    
    // Kiểm tra tên thuộc tính đã tồn tại
    boolean existsByName(String name);
}
