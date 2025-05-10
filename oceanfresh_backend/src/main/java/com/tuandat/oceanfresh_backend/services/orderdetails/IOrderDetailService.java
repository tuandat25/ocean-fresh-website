package com.tuandat.oceanfresh_backend.services.orderdetails;

import java.util.List;

import com.tuandat.oceanfresh_backend.dtos.OrderDetailDTO;
import com.tuandat.oceanfresh_backend.exceptions.DataNotFoundException;
import com.tuandat.oceanfresh_backend.models.OrderDetail;

public interface IOrderDetailService {
    OrderDetail createOrderDetail(OrderDetailDTO newOrderDetail) throws Exception;
    OrderDetail getOrderDetail(Long id) throws DataNotFoundException;
    OrderDetail updateOrderDetail(Long id, OrderDetailDTO newOrderDetailData)
            throws DataNotFoundException;
    void deleteById(Long id);
    List<OrderDetail> findByOrderId(Long orderId);


}
