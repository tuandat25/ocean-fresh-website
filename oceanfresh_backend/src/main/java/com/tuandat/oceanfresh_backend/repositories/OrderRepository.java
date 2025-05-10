package com.tuandat.oceanfresh_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tuandat.oceanfresh_backend.models.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
}
