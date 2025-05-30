package com.tuandat.oceanfresh_backend.services.orders;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tuandat.oceanfresh_backend.dtos.OrderDTO;
import com.tuandat.oceanfresh_backend.exceptions.ResourceNotFoundException;
import com.tuandat.oceanfresh_backend.responses.orders.OrderResponse;

public interface IOrderService {
    OrderResponse createOrder(OrderDTO orderDTO) throws Exception;
    OrderResponse getOrderById(Long orderId) throws ResourceNotFoundException;
    OrderResponse getOrderByCode(String orderCode) throws ResourceNotFoundException;
    OrderResponse updateOrderStatus(Long orderId, String newStatus) throws Exception;
    OrderResponse updateOrder(Long id, OrderDTO orderDTO) throws ResourceNotFoundException;
    void deleteOrder(Long orderId) throws ResourceNotFoundException;
    List<OrderResponse> getOrdersByUserId(Long userId);
    Page<OrderResponse> getAllOrders(Pageable pageable);
    Page<OrderResponse> getOrdersByKeyword(String keyword, Pageable pageable);

    void cancelOrder(Long orderId, String reason) throws Exception;
    List<OrderResponse> findByUserId(Long userId);
}
