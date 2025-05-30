package com.tuandat.oceanfresh_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tuandat.oceanfresh_backend.models.OrderDetail;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    List<OrderDetail> findByOrderId(Long orderId);
    List<OrderDetail> findByProductVariantId(Long productVariantId);
    
    @Query("SELECT od FROM OrderDetail od WHERE od.order.id IN :orderIds")
    List<OrderDetail> findByOrderIdIn(@Param("orderIds") List<Long> orderIds);
    
    // Các truy vấn thống kê
    @Query("SELECT SUM(od.quantity) FROM OrderDetail od WHERE od.productVariant.id = :variantId")
    Integer getTotalSoldQuantityByVariant(@Param("variantId") Long variantId);
}