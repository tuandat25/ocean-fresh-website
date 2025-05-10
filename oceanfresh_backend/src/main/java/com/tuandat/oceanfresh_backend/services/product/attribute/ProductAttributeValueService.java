package com.tuandat.oceanfresh_backend.services.product.attribute;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tuandat.oceanfresh_backend.dtos.ProductAttributeValueDTO;
import com.tuandat.oceanfresh_backend.exceptions.DataNotFoundException;
import com.tuandat.oceanfresh_backend.models.Attribute;
import com.tuandat.oceanfresh_backend.models.Product;
import com.tuandat.oceanfresh_backend.models.ProductAttributeValue;
import com.tuandat.oceanfresh_backend.repositories.AttributeRepository;
import com.tuandat.oceanfresh_backend.repositories.ProductAttributeValueRepository;
import com.tuandat.oceanfresh_backend.repositories.ProductRepository;
import com.tuandat.oceanfresh_backend.responses.ProductAttributeValueResponse;

import lombok.RequiredArgsConstructor;

/**
 * Service để quản lý các giá trị thuộc tính của sản phẩm
 * Hỗ trợ cả thuộc tính đơn giá trị và đa giá trị theo mô hình EAV
 */
@Service
@RequiredArgsConstructor
public class ProductAttributeValueService implements IProductAttributeValueService {
    
    private final ProductAttributeValueRepository productAttributeValueRepository;
    private final ProductRepository productRepository;
    private final AttributeRepository attributeRepository;
    
    @Override
    @Transactional
    public ProductAttributeValue createProductAttributeValue(ProductAttributeValueDTO dto) throws DataNotFoundException {
        // Lấy sản phẩm theo ID
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new DataNotFoundException("Không thể tìm thấy sản phẩm với id: " + dto.getProductId()));
        
        // Lấy thuộc tính theo ID
        Attribute attribute = attributeRepository.findById(dto.getAttributeId())
                .orElseThrow(() -> new DataNotFoundException("Không thể tìm thấy thuộc tính với id: " + dto.getAttributeId()));
        
        // Kiểm tra xem đã tồn tại giá trị cho thuộc tính này của sản phẩm chưa
        ProductAttributeValue existingValue = productAttributeValueRepository
                .findByProductIdAndAttributeId(dto.getProductId(), dto.getAttributeId());
        
        if (existingValue != null) {
            // Nếu đã tồn tại, cập nhật giá trị
            existingValue.setValue(dto.getValue());
            existingValue.setUpdatedAt(LocalDateTime.now());
            return productAttributeValueRepository.save(existingValue);
        }
        
        // Tạo mới nếu chưa tồn tại
        ProductAttributeValue attributeValue = new ProductAttributeValue();
        attributeValue.setProduct(product);
        attributeValue.setAttribute(attribute);
        attributeValue.setValue(dto.getValue());
        attributeValue.setCreatedAt(LocalDateTime.now());
        attributeValue.setUpdatedAt(LocalDateTime.now());
        
        return productAttributeValueRepository.save(attributeValue);
    }
    
    @Override
    public ProductAttributeValue getProductAttributeValueById(Long id) throws DataNotFoundException {
        Optional<ProductAttributeValue> optionalValue = productAttributeValueRepository.findById(id);
        if (optionalValue.isPresent()) {
            return optionalValue.get();
        }
        throw new DataNotFoundException("Không thể tìm thấy thuộc tính sản phẩm với id: " + id);
    }
    
    @Override
    public List<ProductAttributeValueResponse> getProductAttributeValues(Long productId) {
        List<ProductAttributeValue> values = productAttributeValueRepository.findByProductId(productId);
        return values.stream()
                .map(ProductAttributeValueResponse::fromProductAttributeValue)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public ProductAttributeValue updateProductAttributeValue(Long id, ProductAttributeValueDTO dto) throws DataNotFoundException {
        ProductAttributeValue existingValue = getProductAttributeValueById(id);
        
        // Nếu có thay đổi sản phẩm
        if (dto.getProductId() != null && !dto.getProductId().equals(existingValue.getProduct().getId())) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new DataNotFoundException("Cannot find product with id: " + dto.getProductId()));
            existingValue.setProduct(product);
        }
        
        // Nếu có thay đổi thuộc tính
        if (dto.getAttributeId() != null && !dto.getAttributeId().equals(existingValue.getAttribute().getId())) {
            Attribute attribute = attributeRepository.findById(dto.getAttributeId())
                    .orElseThrow(() -> new DataNotFoundException("Cannot find attribute with id: " + dto.getAttributeId()));
            existingValue.setAttribute(attribute);
        }
        
        // Cập nhật giá trị
        if (dto.getValue() != null) {
            existingValue.setValue(dto.getValue());
        }
        
        existingValue.setUpdatedAt(LocalDateTime.now());
        return productAttributeValueRepository.save(existingValue);
    }
    
    @Override
    @Transactional
    public void deleteProductAttributeValue(Long id) throws DataNotFoundException {
        ProductAttributeValue value = getProductAttributeValueById(id);
        productAttributeValueRepository.delete(value);
    }
    
    @Override
    @Transactional
    public void deleteAllProductAttributeValues(Long productId) {
        productAttributeValueRepository.deleteByProductId(productId);
    }
      @Override
    public ProductAttributeValue getByProductIdAndAttributeId(Long productId, Long attributeId) throws DataNotFoundException {
        ProductAttributeValue value = productAttributeValueRepository.findByProductIdAndAttributeId(productId, attributeId);
        if (value == null) {
            throw new DataNotFoundException("Cannot find product attribute value for product id: " + productId + " and attribute id: " + attributeId);
        }
        return value;
    }
    
    @Override
    public List<ProductAttributeValueResponse> getAttributeValuesForProductAndAttribute(Long productId, Long attributeId) {
        List<ProductAttributeValue> values = productAttributeValueRepository.findAllByProductIdAndAttributeId(productId, attributeId);
        return values.stream()
                .map(ProductAttributeValueResponse::fromProductAttributeValue)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void deleteAttributeValuesForProductAndAttribute(Long productId, Long attributeId) {
        List<ProductAttributeValue> values = productAttributeValueRepository.findAllByProductIdAndAttributeId(productId, attributeId);
        if (values != null && !values.isEmpty()) {
            productAttributeValueRepository.deleteAll(values);
        }
    }
}
