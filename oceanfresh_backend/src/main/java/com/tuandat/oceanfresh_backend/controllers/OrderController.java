package com.tuandat.oceanfresh_backend.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tuandat.oceanfresh_backend.dtos.OrderDTO;
import com.tuandat.oceanfresh_backend.models.Order;
import com.tuandat.oceanfresh_backend.responses.orders.OrderResponse;
import com.tuandat.oceanfresh_backend.services.orders.IOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/orders")
public class OrderController {
    private final IOrderService orderService;

    // Tạo đơn hàng mới
    @PostMapping("")
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody OrderDTO orderDTO,
            BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body("Validation errors: " + errorMessages);
            }
            OrderResponse orderResponse = orderService.createOrder(orderDTO);
            return ResponseEntity.ok().body(orderResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi tạo đơn hàng: " + e.getMessage());
        }
    }

    // Lấy danh sách đơn hàng theo user_id
    @GetMapping("/user/{user_id}")
    public ResponseEntity<?> getOrdersByUserId(@PathVariable("user_id") Long userId) {
        try {
            List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
            return ResponseEntity.ok().body(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi lấy danh sách đơn hàng: " + e.getMessage());
        }
    }

    // Lấy chi tiết đơn hàng theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable("id") Long id) {
        try {
            Order orderResponse = orderService.getOrderById(id);
            return ResponseEntity.ok().body(orderResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi lấy chi tiết đơn hàng: " + e.getMessage());
        }
    }

    // Lấy tất cả đơn hàng với phân trang
    @GetMapping("")
    public ResponseEntity<?> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderResponse> orderPage = orderService.getAllOrders(pageable);
            return ResponseEntity.ok().body(orderPage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi lấy danh sách đơn hàng: " + e.getMessage());
        }
    }

    // Cập nhật đơn hàng
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(
            @PathVariable("id") Long id,
            @Valid @RequestBody OrderDTO orderDTO,
            BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body("Validation errors: " + errorMessages);
            }
            Order updatedOrder = orderService.updateOrder(id, orderDTO);
            return ResponseEntity.ok().body(updatedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi cập nhật đơn hàng: " + e.getMessage());
        }
    }

    // Cập nhật trạng thái đơn hàng
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable("id") Long id,
            @RequestParam String status) {
        try {
            Order updatedOrder = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok().body(updatedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi cập nhật trạng thái đơn hàng: " + e.getMessage());
        }
    }

    // Hủy đơn hàng (soft delete)
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable("id") Long id) {
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.ok().body("Đã hủy đơn hàng thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi hủy đơn hàng: " + e.getMessage());
        }
    }
}
