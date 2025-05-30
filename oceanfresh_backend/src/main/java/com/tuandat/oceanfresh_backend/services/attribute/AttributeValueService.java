package com.tuandat.oceanfresh_backend.services.attribute;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tuandat.oceanfresh_backend.dtos.attribute.AttributeValueDTO;
import com.tuandat.oceanfresh_backend.exceptions.DuplicateResourceException;
import com.tuandat.oceanfresh_backend.exceptions.ResourceNotFoundException;
import com.tuandat.oceanfresh_backend.models.Attribute;
import com.tuandat.oceanfresh_backend.models.AttributeValue;
import com.tuandat.oceanfresh_backend.repositories.AttributeRepository;
import com.tuandat.oceanfresh_backend.repositories.AttributeValueRepository;
import com.tuandat.oceanfresh_backend.responses.AttributeValueResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttributeValueService implements IAttributeValueService {
    private static final Logger logger = LoggerFactory.getLogger(AttributeValueService.class);
    private final AttributeValueRepository attributeValueRepository;
    private final AttributeRepository attributeRepository;

    @Override
    @Transactional
    public AttributeValueResponse createAttributeValue(AttributeValueDTO attributeValueDTO) {
        // Tìm thuộc tính cha dựa trên attributeId
        Attribute attribute = attributeRepository.findById(attributeValueDTO.getAttributeId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Thuộc tính", "ID", attributeValueDTO.getAttributeId()));

        // Kiểm tra giá trị đã tồn tại chưa
        if (attributeValueRepository
                .findByAttributeIdAndValue(attributeValueDTO.getAttributeId(), attributeValueDTO.getValue())
                .isPresent()) {
            throw new DuplicateResourceException("Giá trị thuộc tính", "giá trị", attributeValueDTO.getValue());
        }

        // Tạo đối tượng AttributeValue từ DTO
        AttributeValue attributeValue = new AttributeValue();
        attributeValue.setAttribute(attribute);
        attributeValue.setValue(attributeValueDTO.getValue());
        attributeValue
                .setDisplayOrder(attributeValueDTO.getDisplayOrder() == null ? 0 : attributeValueDTO.getDisplayOrder());

        // Lưu vào cơ sở dữ liệu
        AttributeValue savedAttributeValue = attributeValueRepository.save(attributeValue);
        logger.info("Giá trị thuộc tính được tạo với ID: {} cho Thuộc tính ID: {}", savedAttributeValue.getId(),
                attribute.getId());

        // Trả về response
        return AttributeValueResponse.fromAttributeValue(savedAttributeValue);
    }

    @Override
    @Transactional(readOnly = true)
    public AttributeValueResponse getAttributeValueById(Long valueId) {
        AttributeValue attributeValue = attributeValueRepository.findById(valueId)
                .orElseThrow(() -> new ResourceNotFoundException("Giá trị thuộc tính", "ID", valueId));
        return AttributeValueResponse.fromAttributeValue(attributeValue);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeValueResponse> getAttributeValuesByAttributeId(Long attributeId) {
        // Kiểm tra thuộc tính có tồn tại không
        if (!attributeRepository.existsById(attributeId)) {
            throw new ResourceNotFoundException("Thuộc tính", "ID", attributeId);
        }

        // Lấy danh sách các giá trị thuộc tính
        List<AttributeValue> attributeValues = attributeValueRepository.findByAttributeId(attributeId);

        // Chuyển đổi sang DTO và trả về
        return attributeValues.stream()
                .map(AttributeValueResponse::fromAttributeValue)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AttributeValueResponse updateAttributeValue(Long valueId, AttributeValueDTO attributeValueDTO) {
        // Tìm giá trị thuộc tính cần cập nhật
        AttributeValue existingValue = attributeValueRepository.findById(valueId)
                .orElseThrow(() -> new ResourceNotFoundException("Giá trị thuộc tính", "ID", valueId));

        // Kiểm tra nếu attributeId được cung cấp và khác với Attribute hiện tại
        if (attributeValueDTO.getAttributeId() != null &&
                !attributeValueDTO.getAttributeId().equals(existingValue.getAttribute().getId())) {
            throw new ResourceNotFoundException("Không thể thay đổi thuộc tính cha của giá trị thuộc tính");
        }

        // Kiểm tra nếu value thay đổi và đã tồn tại cho thuộc tính đó
        if (attributeValueDTO.getValue() != null &&
                !attributeValueDTO.getValue().equals(existingValue.getValue()) &&
                attributeValueRepository
                        .findByAttributeIdAndValue(existingValue.getAttribute().getId(), attributeValueDTO.getValue())
                        .isPresent()) {
            throw new DuplicateResourceException("Giá trị thuộc tính", "giá trị", attributeValueDTO.getValue());
        }

        // Cập nhật các trường
        if (attributeValueDTO.getValue() != null) {
            existingValue.setValue(attributeValueDTO.getValue());
        }

        if (attributeValueDTO.getDisplayOrder() != null) {
            existingValue.setDisplayOrder(attributeValueDTO.getDisplayOrder());
        }

        // Lưu vào cơ sở dữ liệu
        AttributeValue updatedValue = attributeValueRepository.save(existingValue);
        logger.info("Giá trị thuộc tính được cập nhật với ID: {}", updatedValue.getId());

        // Trả về response
        return AttributeValueResponse.fromAttributeValue(updatedValue);
    }

    @Override
    @Transactional
    public void deleteAttributeValue(Long valueId) {
        if (!attributeValueRepository.existsById(valueId)) {
            throw new ResourceNotFoundException("Giá trị thuộc tính", "ID", valueId);
        }
        attributeValueRepository.deleteById(valueId);
        logger.info("Giá trị thuộc tính được xóa với ID: {}", valueId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByAttributeIdAndValue(Long attributeId, String value) {
        return attributeValueRepository.findByAttributeIdAndValue(attributeId, value).isPresent();
    }
}
