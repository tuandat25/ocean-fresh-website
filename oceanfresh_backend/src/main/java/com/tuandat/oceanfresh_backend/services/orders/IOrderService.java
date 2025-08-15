package com.tuandat.oceanfresh_backend.services.orders;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tuandat.oceanfresh_backend.dtos.OrderDTO;
import com.tuandat.oceanfresh_backend.dtos.OrderUpdateDTO;
import com.tuandat.oceanfresh_backend.exceptions.DataNotFoundException;
import com.tuandat.oceanfresh_backend.exceptions.ResourceNotFoundException;
import com.tuandat.oceanfresh_backend.models.Order;
import com.tuandat.oceanfresh_backend.responses.orders.OrderResponse;

public interface IOrderService {
    OrderResponse createOrder(OrderDTO orderDTO) throws Exception;
    Order getOrderById(Long orderId) throws ResourceNotFoundException;
    OrderResponse getOrderByIdWithDetails(Long orderId) throws ResourceNotFoundException; // Thêm method mới
    OrderResponse getOrderByCode(String orderCode) throws ResourceNotFoundException;
    OrderResponse updateOrderStatus(Long id, String status) throws DataNotFoundException;
    OrderResponse updateOrderStatusAndPaymentStatusByVnPayOrderId(String id, String orderStatus, String paymentStatus) throws DataNotFoundException;
    Order updateOrder(Long id, OrderDTO orderDTO) throws DataNotFoundException;
    OrderResponse updateOrderByAdmin(Long orderId, OrderUpdateDTO orderUpdateDTO) throws ResourceNotFoundException;
    void deleteOrder(Long orderId);
    List<OrderResponse> getOrdersByUserId(Long userId);
    Page<OrderResponse> getAllOrders(Pageable pageable);
    Page<Order> getOrdersByKeyword(String keyword, Pageable pageable);

    OrderResponse cancelOrder(Long orderId, String reason) throws Exception;
    List<OrderResponse> findByUserId(Long userId);
}
