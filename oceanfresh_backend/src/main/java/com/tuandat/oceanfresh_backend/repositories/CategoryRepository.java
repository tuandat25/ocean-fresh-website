package com.tuandat.oceanfresh_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tuandat.oceanfresh_backend.models.Category;

import com.tuandat.oceanfresh_backend.models.Product;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name); // Custom query method to find a category by its name
    Optional<Category> findById(Long id); // Custom query method to find a category by its ID
    //Query find products by category
    @Query("SELECT p FROM Product p WHERE p.category = :category")
    List<Product> findProductByCategory(Category category); // Uncomment if needed for
}
