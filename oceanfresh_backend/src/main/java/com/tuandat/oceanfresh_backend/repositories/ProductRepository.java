package com.tuandat.oceanfresh_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tuandat.oceanfresh_backend.models.Category;
import com.tuandat.oceanfresh_backend.models.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    @Override
    @EntityGraph(attributePaths = { "category", "variants", "variants.selectedAttributes",
            "variants.selectedAttributes.attribute" })
    Optional<Product> findById(Long id);

    // findByCategory
    List<Product> findByCategory(Category category);

    // Query lấy danh sách sản phẩm nếu trạng thái là 1
    @Query("SELECT p FROM Product p WHERE p.isActive = true")
    Page<Product> findAllByIsActiveTrue(Pageable pageable);
}