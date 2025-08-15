package com.tuandat.oceanfresh_backend.services.attribute;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tuandat.oceanfresh_backend.dtos.attribute.AttributeCreateDTO;
import com.tuandat.oceanfresh_backend.dtos.attribute.AttributeDTO;
import com.tuandat.oceanfresh_backend.dtos.attribute.AttributeValueCreateDTO;
import com.tuandat.oceanfresh_backend.exceptions.DuplicateResourceException;
import com.tuandat.oceanfresh_backend.exceptions.ResourceNotFoundException;
import com.tuandat.oceanfresh_backend.models.Attribute;
import com.tuandat.oceanfresh_backend.models.AttributeValue;
import com.tuandat.oceanfresh_backend.repositories.AttributeRepository;
import com.tuandat.oceanfresh_backend.repositories.AttributeValueRepository;
import com.tuandat.oceanfresh_backend.responses.attribute.AttributeWithValuesResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttributeService implements IAttributeService {
    private static final Logger logger = LoggerFactory.getLogger(AttributeService.class);
    private final AttributeRepository attributeRepository;
    private final AttributeValueRepository attributeValueRepository; 
    private final ModelMapper modelMapper; 

    @Override
    @Transactional
    public AttributeWithValuesResponse createAttribute(AttributeCreateDTO attributeDTO) {
        // Kiểm tra xem tên hoặc mã thuộc tính đã tồn tại chưa
        if (attributeRepository.existsByNameOrCode(attributeDTO.getAttributeName(), attributeDTO.getAttributeCode())) {
            throw new DuplicateResourceException("Thuộc tính", "tên hoặc mã", 
                attributeDTO.getAttributeName() + " hoặc " + attributeDTO.getAttributeCode());
        }

        // Tạo attribute
        Attribute attribute = Attribute.builder()
                .name(attributeDTO.getAttributeName())
                .code(attributeDTO.getAttributeCode())
                .description(attributeDTO.getAttributeDescription())
                .build();
        
        Attribute savedAttribute = attributeRepository.save(attribute);
        logger.info("Created attribute with ID: {} and code: {}", savedAttribute.getId(), savedAttribute.getCode());

        // Tạo các attribute values
        for (AttributeValueCreateDTO valueDTO : attributeDTO.getValues()) {
            AttributeValue attributeValue = AttributeValue.builder()
                    .attribute(savedAttribute)
                    .value(valueDTO.getValue())
                    .displayOrder(valueDTO.getDisplayOrder() != null ? valueDTO.getDisplayOrder() : 0)
                    .build();
            
            attributeValueRepository.save(attributeValue);
            // logger.info("Created attribute value: {} for attribute: {}", valueDTO.getValue(), savedAttribute.getCode());
        }

        // Lấy lại attribute để trả về
        Attribute refreshedAttribute = attributeRepository.findById(savedAttribute.getId())
                .orElse(savedAttribute);
        
        return AttributeWithValuesResponse.fromAttribute(refreshedAttribute);
    }

    @Override
    @Transactional(readOnly = true)
    public AttributeDTO getAttributeById(Long id) throws ResourceNotFoundException {
        Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thuộc tính", "ID", id));
        return convertToDTO(attribute);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeDTO> getAllAttributes() {
        List<Attribute> attributes = attributeRepository.findAll();
        return attributes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AttributeDTO updateAttribute(Long id, AttributeDTO attributeDTO) throws ResourceNotFoundException {
        Attribute existingAttribute = attributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thuộc tính", "ID", id));

        if (StringUtils.hasText(attributeDTO.getName()) && !existingAttribute.getName().equals(attributeDTO.getName())
                &&
                attributeRepository.existsByNameAndIdNot(attributeDTO.getName(), id)) {
            throw new DuplicateResourceException("Thuộc tính", "tên", attributeDTO.getName());
        }

        if (StringUtils.hasText(attributeDTO.getCode()) && !existingAttribute.getCode().equals(attributeDTO.getCode())
                &&
                attributeRepository.existsByCodeAndIdNot(attributeDTO.getCode(), id)) {
            throw new DuplicateResourceException("Thuộc tính", "mã", attributeDTO.getCode());
        }

        existingAttribute.setName(attributeDTO.getName());
        existingAttribute.setCode(attributeDTO.getCode());
        existingAttribute.setDescription(attributeDTO.getDescription());
        // Timestamps are handled by BaseEntity

        Attribute updatedAttribute = attributeRepository.save(existingAttribute);
        logger.info("Thuộc tính được cập nhật với ID: {}", updatedAttribute.getId());
        return convertToDTO(updatedAttribute);
    }

    @Override
    @Transactional
    public void deleteAttribute(Long id) throws ResourceNotFoundException {
        if (!attributeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Thuộc tính", "ID", id);
        }
        attributeRepository.deleteById(id);
        logger.info("Thuộc tính được xóa với ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return attributeRepository.existsByName(name);
    }

    // Helper methods for DTO conversion
    private AttributeDTO convertToDTO(Attribute attribute) {
        return modelMapper.map(attribute, AttributeDTO.class);
    }

    @Override
    public AttributeDTO getAttributeByCode(String code) throws ResourceNotFoundException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAttributeByCode'");
    }
}
