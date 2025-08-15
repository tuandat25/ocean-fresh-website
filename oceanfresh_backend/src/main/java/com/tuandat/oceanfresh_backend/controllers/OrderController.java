package com.tuandat.oceanfresh_backend.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.tuandat.oceanfresh_backend.dtos.OrderUpdateDTO;
import com.tuandat.oceanfresh_backend.models.Order;
import com.tuandat.oceanfresh_backend.responses.ResponseObject;
import com.tuandat.oceanfresh_backend.responses.orders.OrderResponse;
import com.tuandat.oceanfresh_backend.services.orders.IOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/orders")
public class OrderController {
    private final IOrderService orderService;

    // Tạo đơn hàng mới - chỉ user đăng nhập
    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody OrderDTO orderDTO,
            BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body("Lỗi: " + errorMessages);
            }
            OrderResponse orderResponse = orderService.createOrder(orderDTO);
            return ResponseEntity.ok().body(orderResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi tạo đơn hàng: " + e.getMessage());
        }
    }

    // Lấy danh sách đơn hàng theo user_id - user chỉ có thể xem đơn hàng của mình
    @GetMapping("/user/{user_id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and #userId == authentication.principal.id)")
    public ResponseEntity<?> getOrdersByUserId(@PathVariable("user_id") Long userId) {
        try {
            List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
            return ResponseEntity.ok(ResponseObject.builder()
                                        .message("Lấy danh sách đơn hàng thành công")
                                        .status(HttpStatus.OK)
                                        .data(orders)
                                        .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi lấy danh sách đơn hàng: " + e.getMessage());
        }
    }

    // Lấy chi tiết đơn hàng theo ID - user chỉ có thể xem đơn hàng của mình
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @orderService.isOrderOwner(#id, authentication.principal.id)")
    public ResponseEntity<?> getOrderById(@PathVariable("id") Long id) {
        try {
            OrderResponse orderResponse = orderService.getOrderByIdWithDetails(id);
            return ResponseEntity.ok(ResponseObject.builder()
                                        .message("Lấy chi tiết đơn hàng thành công")
                                        .status(HttpStatus.OK)
                                        .data(orderResponse)
                                        .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi lấy chi tiết đơn hàng: " + e.getMessage());
        }
    }

    @GetMapping("/get-by-order-code/{orderCode}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @orderService.isOrderOwner(#orderCode, authentication.principal.id)")
    public ResponseEntity<?> getOrderByOrderCode(@PathVariable("orderCode") String orderCode) {
        try {
            OrderResponse orderResponse = orderService.getOrderByCode(orderCode);
            return ResponseEntity.ok(ResponseObject.builder()
                                        .message("Lấy chi tiết đơn hàng thành công")
                                        .status(HttpStatus.OK)
                                        .data(orderResponse)
                                        .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi lấy chi tiết đơn hàng: " + e.getMessage());
        }
    }

    // Lấy tất cả đơn hàng với phân trang - chỉ admin
    @GetMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderResponse> orderPage = orderService.getAllOrders(pageable);
            return ResponseEntity.ok(ResponseObject.builder()
                                        .message("Lấy danh sách đơn hàng thành công")
                                        .status(HttpStatus.OK)
                                        .data(orderPage)
                                        .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi lấy danh sách đơn hàng: " + e.getMessage());
        }
    }

    // Cập nhật đơn hàng - user chỉ có thể cập nhật đơn hàng của mình
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @orderService.isOrderOwner(#id, authentication.principal.id)")
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
    // Cập nhật đơn hàng - admin có thể cập nhật mọi đơn hàng
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateOrderByAdmin(
            @PathVariable("id") Long id,
            @Valid @RequestBody OrderUpdateDTO orderDTO,
            BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body("Validation errors: " + errorMessages);
            }
            OrderResponse updatedOrder = orderService.updateOrderByAdmin(id, orderDTO);
            return ResponseEntity.ok(ResponseObject.builder()
                                        .message("Cập nhật đơn hàng thành công")
                                        .status(HttpStatus.OK)
                                        .data(updatedOrder)
                                        .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi cập nhật đơn hàng: " + e.getMessage());
        }
    }

    // Cập nhật trạng thái đơn hàng - admin có thể cập nhật mọi trạng thái, user chỉ được cập nhật một số trạng thái
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @orderService.isOrderOwner(#id, authentication.principal.id)")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable("id") Long id,
            @RequestParam String status) {
        try {
            OrderResponse updatedOrder = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(ResponseObject.builder()
                                        .message("Cập nhật trạng thái đơn hàng thành công")
                                        .status(HttpStatus.OK)
                                        .data(updatedOrder)
                                        .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi cập nhật trạng thái đơn hàng: " + e.getMessage());
        }
    }

    //Chỉnh lại endpoint này để cập nhật trạng thái đơn hàng và trạng thái thanh toán từ VNPAY
    @PutMapping("/payment/vnpay/{vnpTxnRef}")
    // @PreAuthorize("hasRole('ROLE_ADMIN') or @orderService.isOrderOwner(#id, authentication.principal.id)")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> updateOrderStatusAndPaymentStatus(
            @PathVariable("vnpTxnRef") String vnpTxnRef,
            @RequestParam String orderStatus,
            @RequestParam(required = false) String paymentStatus) {
        try {
            OrderResponse updatedOrder = orderService.updateOrderStatusAndPaymentStatusByVnPayOrderId(vnpTxnRef, orderStatus, paymentStatus);
            return ResponseEntity.ok(ResponseObject.builder()
                                        .message("Cập nhật trạng thái đơn hàng thành công")
                                        .status(HttpStatus.OK)
                                        .data(updatedOrder)
                                        .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi cập nhật trạng thái đơn hàng: " + e.getMessage());
        }
    }

    // Hủy đơn hàng (soft delete) - user chỉ có thể hủy đơn hàng của mình
    @PutMapping("/admin/cancel/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteOrder(@PathVariable("id") Long id) {
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.ok().body("Đã hủy đơn hàng thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi hủy đơn hàng: " + e.getMessage());
        }
    }


    @PutMapping("/cancel/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @orderService.isOrderOwner(#id, authentication.principal.id)")
    public ResponseEntity<?> cancelOrder(@PathVariable("id") Long id) {
        try {
            OrderResponse response = orderService.cancelOrder(id, "");
            return ResponseEntity.ok(ResponseObject.builder()
                                        .message("Hủy đơn hàng thành công")
                                        .status(HttpStatus.OK)
                                        .data(response)
                                        .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi hủy đơn hàng: " + e.getMessage());
        }
    }
}
