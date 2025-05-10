package com.tuandat.oceanfresh_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tuandat.oceanfresh_backend.models.ProductAttributeValue;

@Repository
public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue, Long> {
    List<ProductAttributeValue> findByProductId(Long productId);
    List<ProductAttributeValue> findByAttributeId(Long attributeId);
    
    // Tìm thuộc tính của sản phẩm theo attribute ID
    ProductAttributeValue findByProductIdAndAttributeId(Long productId, Long attributeId);
    
    // Tìm tất cả giá trị của một thuộc tính cho một sản phẩm
    List<ProductAttributeValue> findAllByProductIdAndAttributeId(Long productId, Long attributeId);
    
    // Xóa tất cả thuộc tính của một sản phẩm
    void deleteByProductId(Long productId);
}
