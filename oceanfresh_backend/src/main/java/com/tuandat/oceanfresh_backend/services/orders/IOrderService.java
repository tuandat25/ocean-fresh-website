package com.tuandat.oceanfresh_backend.services.orders;

import java.util.List;

import com.tuandat.oceanfresh_backend.dtos.OrderDTO;
import com.tuandat.oceanfresh_backend.exceptions.ResourceNotFoundException;
import com.tuandat.oceanfresh_backend.responses.OrderResponse;

// import com.tuandat.oceanfresh_backend.exceptions.ResourceNotFoundException;
// import com.tuandat.oceanfresh_backend.models.Order;
// import com.tuandat.oceanfresh_backend.responses.OrderResponse;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;

// import java.util.List;

public interface IOrderService {
    OrderResponse createOrder(OrderDTO orderDTO) throws Exception;
    OrderResponse getOrderById(Long orderId) throws ResourceNotFoundException;
    OrderResponse updateOrder(Long id, OrderDTO orderDTO) throws ResourceNotFoundException;
    void deleteOrder(Long orderId) throws ResourceNotFoundException;
    List<OrderResponse> findByUserId(Long userId);
//     Page<Order> getOrdersByKeyword(String keyword, Pageable pageable);
//     // Thêm phương thức cập nhật trạng thái đơn hàng
//     Order updateOrderStatus(Long id, String status) throws ResourceNotFoundException;
}
