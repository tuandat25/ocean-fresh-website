package com.tuandat.oceanfresh_backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tuandat.oceanfresh_backend.models.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    Optional<ProductVariant> findBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long id); // Kiểm tra SKU tồn tại cho biến thể khác

    List<ProductVariant> findByProductId(Long productId);

    @Query("SELECT pv FROM ProductVariant pv JOIN pv.selectedAttributes sa " +
           "WHERE pv.product.id = :productId AND sa.id IN :attributeValueIds " +
           "GROUP BY pv.id " + // Thêm các trường khác của pv nếu cần SELECT
           "HAVING COUNT(DISTINCT sa.id) = :attributeValueCount")
    List<ProductVariant> findVariantsByProductAndAttributeValues(
            @Param("productId") Long productId,
            @Param("attributeValueIds") Set<Long> attributeValueIds,
            @Param("attributeValueCount") long attributeValueCount
    );
}