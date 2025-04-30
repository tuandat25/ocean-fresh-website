package com.tuandat.oceanfresh_backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tuandat.oceanfresh_backend.models.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name); // Custom query method to find a category by its name
    Optional<Category> findById(Long id); // Custom query method to find a category by its ID

}
