package com.tuandat.oceanfresh_backend.services.attribute;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tuandat.oceanfresh_backend.dtos.AttributeDTO;
import com.tuandat.oceanfresh_backend.exceptions.DataNotFoundException;
import com.tuandat.oceanfresh_backend.models.Attribute;
import com.tuandat.oceanfresh_backend.repositories.AttributeRepository;
import com.tuandat.oceanfresh_backend.responses.AttributeResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttributeService implements IAttributeService {
    
    private final AttributeRepository attributeRepository;
    
    @Override
    @Transactional
    public Attribute createAttribute(AttributeDTO attributeDTO) {
        Attribute attribute = new Attribute();
        attribute.setName(attributeDTO.getName());
        attribute.setCreatedAt(LocalDateTime.now());
        attribute.setUpdatedAt(LocalDateTime.now());
        return attributeRepository.save(attribute);
    }
    
    @Override
    public Attribute getAttributeById(Long id) throws DataNotFoundException {
        Optional<Attribute> optionalAttribute = attributeRepository.findById(id);
        if (optionalAttribute.isPresent()) {
            return optionalAttribute.get();
        }
        throw new DataNotFoundException("Không tìm thấy thuộc tính: " + id);
    }
    
    @Override
    public List<AttributeResponse> getAllAttributes() {
        List<Attribute> attributes = attributeRepository.findAll();
        return attributes.stream()
                .map(AttributeResponse::fromAttribute)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public Attribute updateAttribute(Long id, AttributeDTO attributeDTO) throws DataNotFoundException {
        Attribute existingAttribute = getAttributeById(id);
        existingAttribute.setName(attributeDTO.getName());
        existingAttribute.setUpdatedAt(LocalDateTime.now());
        return attributeRepository.save(existingAttribute);
    }
    
    @Override
    @Transactional
    public void deleteAttribute(Long id) throws DataNotFoundException {
        Attribute attribute = getAttributeById(id);
        attributeRepository.delete(attribute);
    }
    
    @Override
    public boolean existsByName(String name) {
        return attributeRepository.existsByName(name);
    }
}
