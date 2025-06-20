package com.tuandat.oceanfresh_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tuandat.oceanfresh_backend.models.OrderDetail;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    List<OrderDetail> findByOrderId(Long orderId);
}
