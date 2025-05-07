package com.tuandat.oceanfresh_backend.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tuandat.oceanfresh_backend.dtos.OrderDTO;

import jakarta.validation.Valid;


@RestController
@RequestMapping("${api.prefix}/orders")
public class OrderController {
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
            // Call the service to create the order
            // Order order = orderService.createOrder(orderDTO);
            return ResponseEntity.ok("Create order successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Internal server error: " + e.getMessage());
        }
    }

    //Get order by user_id
    @GetMapping("/{user_id}")
    public ResponseEntity<?> getOrders(@Valid @PathVariable("user_id") Long userId) {
        try {
            // Call the service to get the orders by user_id
            // List<Order> orders = orderService.getOrdersByUserId(userId);
            return ResponseEntity.ok("Get orders successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Internal server error: " + e.getMessage());
        }
    }

    //Update order by order_id

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@Valid @PathVariable("id") Long id,
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
            // Call the service to update the order by order_id
            // Order order = orderService.updateOrder(orderId, orderDTO);
            return ResponseEntity.ok("Update order successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Internal server error: " + e.getMessage());
        }
    }

    //Soft delete order by order_id
    @PutMapping("/delete/{id}")
    public ResponseEntity<?> deleteOrder(@Valid @PathVariable("id") Long id) {
        try {
            // Call the service to soft delete the order by order_id
            // Order order = orderService.deleteOrder(orderId);
            return ResponseEntity.ok("Delete order successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Internal server error: " + e.getMessage());
        }
    }
    
}
