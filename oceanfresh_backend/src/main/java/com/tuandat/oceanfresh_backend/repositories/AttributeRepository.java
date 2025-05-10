package com.tuandat.oceanfresh_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tuandat.oceanfresh_backend.models.Attribute;

@Repository
public interface AttributeRepository extends JpaRepository<Attribute, Long> {
    boolean existsByName(String name);
    Attribute findByName(String name);
}
