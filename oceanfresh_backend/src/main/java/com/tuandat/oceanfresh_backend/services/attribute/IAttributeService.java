package com.tuandat.oceanfresh_backend.services.attribute;

import java.util.List;

import com.tuandat.oceanfresh_backend.dtos.attribute.AttributeDTO;
import com.tuandat.oceanfresh_backend.exceptions.ResourceNotFoundException;

public interface IAttributeService {
    // Tạo mới một thuộc tính
    AttributeDTO createAttribute(AttributeDTO attributeDTO);

    // Lấy thuộc tính theo ID
    AttributeDTO getAttributeById(Long id) throws ResourceNotFoundException;
    
    // Lấy thuộc tính theo mã code
    AttributeDTO getAttributeByCode(String code) throws ResourceNotFoundException;

    // Lấy tất cả thuộc tính
    List<AttributeDTO> getAllAttributes();

    // Cập nhật thuộc tính
    AttributeDTO updateAttribute(Long id, AttributeDTO attributeDTO) throws ResourceNotFoundException;

    // Xóa thuộc tính
    void deleteAttribute(Long id) throws ResourceNotFoundException;
    
    // Kiểm tra tên thuộc tính đã tồn tại
    boolean existsByName(String name);
}
