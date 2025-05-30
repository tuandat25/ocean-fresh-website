package com.tuandat.oceanfresh_backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tuandat.oceanfresh_backend.models.AttributeValue;

@Repository
public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {
    Optional<AttributeValue> findByAttributeIdAndValue(Long attributeId, String value);
    List<AttributeValue> findByAttributeId(Long attributeId);
    List<AttributeValue> findByIdIn(Set<Long> ids); // Thay đổi List thành Set
}
