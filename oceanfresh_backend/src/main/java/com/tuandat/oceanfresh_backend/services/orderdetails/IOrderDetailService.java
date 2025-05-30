package com.tuandat.oceanfresh_backend.services.orderdetails;

import java.util.List;

import com.tuandat.oceanfresh_backend.dtos.OrderItemDTO;
import com.tuandat.oceanfresh_backend.exceptions.ResourceNotFoundException;
import com.tuandat.oceanfresh_backend.models.OrderDetail;

public interface IOrderDetailService {
    OrderDetail createOrderDetail(OrderItemDTO newOrderDetail) throws Exception;
    OrderDetail getOrderDetail(Long id) throws ResourceNotFoundException;
    OrderDetail updateOrderDetail(Long id, OrderItemDTO newOrderDetailData)
            throws ResourceNotFoundException;
    void deleteById(Long id);
    List<OrderDetail> findByOrderId(Long orderId);


}
