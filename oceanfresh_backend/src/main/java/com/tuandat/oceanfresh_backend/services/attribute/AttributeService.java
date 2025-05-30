package com.tuandat.oceanfresh_backend.services.attribute;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tuandat.oceanfresh_backend.dtos.attribute.AttributeDTO;
import com.tuandat.oceanfresh_backend.exceptions.DuplicateResourceException;
import com.tuandat.oceanfresh_backend.exceptions.ResourceNotFoundException;
import com.tuandat.oceanfresh_backend.models.Attribute;
import com.tuandat.oceanfresh_backend.repositories.AttributeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttributeService implements IAttributeService {
    private static final Logger logger = LoggerFactory.getLogger(AttributeService.class);
    private final AttributeRepository attributeRepository;
    private final ModelMapper modelMapper; // Added ModelMapper

    @Override
    @Transactional
    public AttributeDTO createAttribute(AttributeDTO attributeDTO) {
        if (attributeRepository.existsByNameOrCode(attributeDTO.getName(), attributeDTO.getCode())) {
            throw new DuplicateResourceException("Thuộc tính", "tên hoặc mã", attributeDTO.getName() + " hoặc " + attributeDTO.getCode());
        }
        Attribute attribute = convertToEntity(attributeDTO);
        // Timestamps are handled by BaseEntity
        Attribute savedAttribute = attributeRepository.save(attribute);
        logger.info("Thuộc tính được tạo với ID: {} và Tên: {}", savedAttribute.getId(), savedAttribute.getName());
        return convertToDTO(savedAttribute);
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

        if (StringUtils.hasText(attributeDTO.getName()) && !existingAttribute.getName().equals(attributeDTO.getName()) &&
            attributeRepository.existsByNameAndIdNot(attributeDTO.getName(), id)) {
            throw new DuplicateResourceException("Thuộc tính", "tên", attributeDTO.getName());
        }

        if (StringUtils.hasText(attributeDTO.getCode()) && !existingAttribute.getCode().equals(attributeDTO.getCode()) &&
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
        // This method might need to be re-evaluated based on whether "name" or "code" or both should be checked.
        // The IAttributeService defines existsByName, so we stick to that.
        // If existsByNameOrCode is more appropriate for general checks, consider adding it to the interface.
        return attributeRepository.existsByNameOrCode(name, null); // Or adjust as per specific requirement for this method
    }    @Override
    @Transactional(readOnly = true)
    public AttributeDTO getAttributeByCode(String code) throws ResourceNotFoundException {
        Attribute attribute = attributeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Thuộc tính", "mã", code));
        return convertToDTO(attribute);
    }

    // Helper methods for DTO conversion
    private AttributeDTO convertToDTO(Attribute attribute) {
        return modelMapper.map(attribute, AttributeDTO.class);
    }

    private Attribute convertToEntity(AttributeDTO attributeDTO) {
        return modelMapper.map(attributeDTO, Attribute.class);
    }
}
