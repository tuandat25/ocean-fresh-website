package com.tuandat.oceanfresh_backend.services.orderdetails;

import com.tuandat.oceanfresh_backend.dtos.OrderDetailDTO;
import com.tuandat.oceanfresh_backend.exceptions.DataNotFoundException;
import com.tuandat.oceanfresh_backend.models.Order;
import com.tuandat.oceanfresh_backend.models.OrderDetail;
import com.tuandat.oceanfresh_backend.models.ProductVariant;
import com.tuandat.oceanfresh_backend.repositories.OrderDetailRepository;
import com.tuandat.oceanfresh_backend.repositories.OrderRepository;
import com.tuandat.oceanfresh_backend.repositories.ProductVariantRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional
public class OrderDetailService implements IOrderDetailService{
    private static final Logger logger = LoggerFactory.getLogger(OrderDetailService.class);
    
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductVariantRepository productVariantRepository;
    
    @Override
    @Transactional
    public OrderDetail createOrderDetail(OrderDetailDTO orderDetailDTO) throws Exception {
        // Tìm xem orderId có tồn tại không
        Order order = orderRepository.findById(orderDetailDTO.getOrderId())
                .orElseThrow(() -> new DataNotFoundException(
                        "Cannot find Order with id : "+orderDetailDTO.getOrderId()));
        
        // Tìm ProductVariant theo id
        ProductVariant productVariant = productVariantRepository.findById(orderDetailDTO.getProductVariantId())
                .orElseThrow(() -> new DataNotFoundException(
                        "Cannot find product variant with id: " + orderDetailDTO.getProductVariantId()));
        
        // Tính toán các giá trị
        BigDecimal priceAtOrder = BigDecimal.valueOf(orderDetailDTO.getPrice());
        int quantity = orderDetailDTO.getNumberOfProducts();
        BigDecimal totalLineAmount = priceAtOrder.multiply(BigDecimal.valueOf(quantity));
        
        OrderDetail orderDetail = OrderDetail.builder()
                .order(order)
                .productVariant(productVariant)
                .quantity(quantity)
                .priceAtOrder(priceAtOrder)
                .totalLineAmount(totalLineAmount)
                .build();
        
        // Lưu vào database
        OrderDetail savedOrderDetail = orderDetailRepository.save(orderDetail);
        logger.info("Created order detail with id: {} for order: {}", 
                   savedOrderDetail.getId(), order.getOrderCode());
        
        return savedOrderDetail;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetail getOrderDetail(Long id) throws DataNotFoundException {
        return orderDetailRepository.findById(id)
                .orElseThrow(()->new DataNotFoundException("Cannot find OrderDetail with id: "+id));
    }

    @Override
    @Transactional
    public OrderDetail updateOrderDetail(Long id, OrderDetailDTO orderDetailDTO)
            throws DataNotFoundException {
        // Tìm xem order detail có tồn tại không
        OrderDetail existingOrderDetail = orderDetailRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Cannot find order detail with id: "+id));
        
        // Tìm Order theo id
        Order existingOrder = orderRepository.findById(orderDetailDTO.getOrderId())
                .orElseThrow(() -> new DataNotFoundException("Cannot find order with id: "+orderDetailDTO.getOrderId()));
        
        // Tìm ProductVariant theo id
        ProductVariant existingProductVariant = productVariantRepository.findById(orderDetailDTO.getProductVariantId())
                .orElseThrow(() -> new DataNotFoundException(
                        "Cannot find product variant with id: " + orderDetailDTO.getProductVariantId()));
        
        // Cập nhật các giá trị
        BigDecimal priceAtOrder = BigDecimal.valueOf(orderDetailDTO.getPrice());
        int quantity = orderDetailDTO.getNumberOfProducts();
        BigDecimal totalLineAmount = priceAtOrder.multiply(BigDecimal.valueOf(quantity));
        
        existingOrderDetail.setPriceAtOrder(priceAtOrder);
        existingOrderDetail.setQuantity(quantity);
        existingOrderDetail.setTotalLineAmount(totalLineAmount);
        existingOrderDetail.setOrder(existingOrder);
        existingOrderDetail.setProductVariant(existingProductVariant);
        
        OrderDetail updatedOrderDetail = orderDetailRepository.save(existingOrderDetail);
        logger.info("Updated order detail with id: {}", updatedOrderDetail.getId());
          return updatedOrderDetail;
    }

    @Override
    @Transactional
    public void deleteById(Long id) throws DataNotFoundException {
        // Check if order detail exists before deleting
        if (orderDetailRepository.existsById(id)) {
            orderDetailRepository.deleteById(id);
            logger.info("Đã xóa hóa đơn với mã: {}", id);
        } else {
            logger.warn("Attempted to delete non-existent order detail with id: {}", id);
            throw new DataNotFoundException("Cannot find OrderDetail with id: " + id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDetail> findByOrderId(Long orderId) throws DataNotFoundException {
        // Validate order exists
        if (!orderRepository.existsById(orderId)) {
            throw new DataNotFoundException("Không tìm thấy đơn hàng với mã: " + orderId);
        }
        
        return orderDetailRepository.findByOrderId(orderId);
    }
}
