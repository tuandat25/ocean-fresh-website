package com.tuandat.oceanfresh_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tuandat.oceanfresh_backend.models.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    // Custom query methods can be defined here if needed
    // For example, to find images by product ID:
    List<ProductImage> findByProductId(Long productId);

}
