package com.tuandat.oceanfresh_backend.services.product.attribute;

import java.util.List;

import com.tuandat.oceanfresh_backend.dtos.ProductAttributeValueDTO;
import com.tuandat.oceanfresh_backend.exceptions.DataNotFoundException;
import com.tuandat.oceanfresh_backend.models.ProductAttributeValue;
import com.tuandat.oceanfresh_backend.responses.ProductAttributeValueResponse;

public interface IProductAttributeValueService {
    // Tạo mới một giá trị thuộc tính cho sản phẩm
    ProductAttributeValue createProductAttributeValue(ProductAttributeValueDTO productAttributeValueDTO) throws DataNotFoundException;
    
    // Lấy giá trị thuộc tính theo ID
    ProductAttributeValue getProductAttributeValueById(Long id) throws DataNotFoundException;
    
    // Lấy tất cả giá trị thuộc tính của một sản phẩm
    List<ProductAttributeValueResponse> getProductAttributeValues(Long productId);
    
    // Cập nhật giá trị thuộc tính
    ProductAttributeValue updateProductAttributeValue(Long id, ProductAttributeValueDTO productAttributeValueDTO) throws DataNotFoundException;
    
    // Xóa giá trị thuộc tính
    void deleteProductAttributeValue(Long id) throws DataNotFoundException;
    
    // Xóa tất cả giá trị thuộc tính của một sản phẩm
    void deleteAllProductAttributeValues(Long productId);
    
    // Lấy giá trị thuộc tính theo sản phẩm và loại thuộc tính
    ProductAttributeValue getByProductIdAndAttributeId(Long productId, Long attributeId) throws DataNotFoundException;
    
    // Lấy tất cả giá trị của một thuộc tính cụ thể cho một sản phẩm
    List<ProductAttributeValueResponse> getAttributeValuesForProductAndAttribute(Long productId, Long attributeId);
    
    // Xóa tất cả giá trị của một thuộc tính cụ thể cho một sản phẩm
    void deleteAttributeValuesForProductAndAttribute(Long productId, Long attributeId);
}
