package com.tuandat.oceanfresh_backend.services.orders;

// import com.tuandat.oceanfresh_backend.dtos.CartItemDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tuandat.oceanfresh_backend.dtos.OrderDTO;
import com.tuandat.oceanfresh_backend.exceptions.ResourceNotFoundException;
import com.tuandat.oceanfresh_backend.models.Order;
import com.tuandat.oceanfresh_backend.models.OrderStatus;
import com.tuandat.oceanfresh_backend.models.User;
import com.tuandat.oceanfresh_backend.repositories.OrderRepository;
import com.tuandat.oceanfresh_backend.repositories.UserRepository;
import com.tuandat.oceanfresh_backend.responses.OrderResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    // private final ProductRepository productRepository;
    // private final CouponRepository couponRepository;
    // private final OrderDetailRepository orderDetailRepository;

    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderDTO orderDTO) throws Exception {

        //tìm xem user'id có tồn tại ko
        User user = userRepository
                .findById(orderDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find user with id: "+orderDTO.getUserId()));
        //convert orderDTO => Order
        //dùng thư viện Model Mapper
        // Tạo một luồng bảng ánh xạ riêng để kiểm soát việc ánh xạ
        modelMapper.typeMap(OrderDTO.class, Order.class)
                .addMappings(mapper -> mapper.skip(Order::setId));
        // Cập nhật các trường của đơn hàng từ orderDTO
        Order order = new Order();
        modelMapper.map(orderDTO, order);
        order.setUser(user);        order.setOrderDate(LocalDateTime.now());//lấy thời điểm hiện tại
        order.setStatus(OrderStatus.PENDING);
        //Kiểm tra shipping date phải >= ngày hôm nay
        LocalDate shippingDate = orderDTO.getShippingDate() == null
                ? LocalDate.now() : orderDTO.getShippingDate();
        if (shippingDate.isBefore(LocalDate.now())) {
            throw new ResourceNotFoundException("Date must be at least today !");
        }
        order.setShippingDateExpected(shippingDate);
        // Note: Orders don't have an active field, they use status enum instead
        // //EAV-Entity-Attribute-Value model
        // order.setTotalMoney(orderDTO.getTotalMoney());
        // // Lưu vnpTxnRef nếu có
        // if (orderDTO.getVnpTxnRef() != null) {
        //     order.setVnpTxnRef(orderDTO.getVnpTxnRef());
        // }
        // if(orderDTO.getShippingAddress() == null) {
        //     order.setShippingAddress(orderDTO.getAddress());
        // }
        // // Tạo danh sách các đối tượng OrderDetail từ cartItems
        // List<OrderDetail> orderDetails = new ArrayList<>();
        // for (CartItemDTO cartItemDTO : orderDTO.getCartItems()) {
        //     // Tạo một đối tượng OrderDetail từ CartItemDTO
        //     OrderDetail orderDetail = new OrderDetail();
        //     orderDetail.setOrder(order);

        //     // Lấy thông tin sản phẩm từ cartItemDTO
        //     Long productId = cartItemDTO.getProductId();
        //     int quantity = cartItemDTO.getQuantity();

        //     // Tìm thông tin sản phẩm từ cơ sở dữ liệu (hoặc sử dụng cache nếu cần)
        //     Product product = productRepository.findById(productId)
        //             .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        //     // Đặt thông tin cho OrderDetail
        //     orderDetail.setProduct(product);
        //     orderDetail.setNumberOfProducts(quantity);
        //     // Các trường khác của OrderDetail nếu cần
        //     orderDetail.setPrice(product.getPrice());

        //     // Thêm OrderDetail vào danh sách
        //     orderDetails.add(orderDetail);
        // }

        // //coupon
        // String couponCode = orderDTO.getCouponCode();
        // if (!couponCode.isEmpty()) {
        //     Coupon coupon = couponRepository.findByCode(couponCode)
        //             .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));

        //     if (!coupon.isActive()) {
        //         throw new IllegalArgumentException("Coupon is not active");
        //     }

        //     order.setCoupon(coupon);
        // } else {
        //     order.setCoupon(null);
        // }
        // // Lưu danh sách OrderDetail vào cơ sở dữ liệu
        // orderDetailRepository.saveAll(orderDetails);
        orderRepository.save(order);
        // return order;
        return modelMapper.map(order, OrderResponse.class);
    }

    // @Transactional
    // public Order updateOrderWithDetails(OrderWithDetailsDTO orderWithDetailsDTO) {
    //     modelMapper.typeMap(OrderWithDetailsDTO.class, Order.class)
    //             .addMappings(mapper -> mapper.skip(Order::setId));
    //     Order order = new Order();
    //     modelMapper.map(orderWithDetailsDTO, order);
    //     Order savedOrder = orderRepository.save(order);

    //     // Set the order for each order detail
    //     for (OrderDetailDTO orderDetailDTO : orderWithDetailsDTO.getOrderDetailDTOS()) {
    //         //orderDetail.setOrder(OrderDetail);
    //     }

    //     // Save or update the order details
    //     List<OrderDetail> savedOrderDetails = orderDetailRepository.saveAll(order.getOrderDetails());

    //     // Set the updated order details for the order
    //     savedOrder.setOrderDetails(savedOrderDetails);

    //     return savedOrder;
    // }
    // @Override
    // public Order getOrderById(Long orderId) {
    //     // Tìm theo ID
    //     Order order = orderRepository.findById(orderId).orElse(null);
    //     if (order == null) {
    //         // Nếu không tìm thấy theo ID, tìm theo vnpTxnRef
    //         order = orderRepository.findByVnpTxnRef(orderId.toString()).orElse(null);
    //     }
    //     return order;
    // }

    // @Override
    // @Transactional
    // public Order updateOrder(Long id, OrderDTO orderDTO)
    //         throws ResourceNotFoundException {
    //     Order order = getOrderById(id);
    //     User existingUser = userRepository.findById(
    //             orderDTO.getUserId()).orElseThrow(() ->
    //             new ResourceNotFoundException("Cannot find user with id: " + id));
    //     /*
    //     modelMapper.typeMap(OrderDTO.class, Order.class)
    //             .addMappings(mapper -> mapper.skip(Order::setId));
    //     modelMapper.map(orderDTO, order);
    //      */
    //     // Setting user
    //     if (orderDTO.getUserId() != null) {
    //         User user = new User();
    //         user.setId(orderDTO.getUserId());
    //         order.setUser(user);
    //     }

    //     if (orderDTO.getFullName() != null && !orderDTO.getFullName().trim().isEmpty()) {
    //         order.setFullName(orderDTO.getFullName().trim());
    //     }

    //     if (orderDTO.getEmail() != null && !orderDTO.getEmail().trim().isEmpty()) {
    //         order.setEmail(orderDTO.getEmail().trim());
    //     }

    //     if (orderDTO.getPhoneNumber() != null && !orderDTO.getPhoneNumber().trim().isEmpty()) {
    //         order.setPhoneNumber(orderDTO.getPhoneNumber().trim());
    //     }

    //     if (orderDTO.getStatus() != null && !orderDTO.getStatus().trim().isEmpty()) {
    //         order.setStatus(orderDTO.getStatus().trim());
    //     }

    //     if (orderDTO.getAddress() != null && !orderDTO.getAddress().trim().isEmpty()) {
    //         order.setAddress(orderDTO.getAddress().trim());
    //     }

    //     if (orderDTO.getNote() != null && !orderDTO.getNote().trim().isEmpty()) {
    //         order.setNote(orderDTO.getNote().trim());
    //     }

    //     if (orderDTO.getTotalMoney() != null) {
    //         order.setTotalMoney(orderDTO.getTotalMoney());
    //     }

    //     if (orderDTO.getShippingMethod() != null && !orderDTO.getShippingMethod().trim().isEmpty()) {
    //         order.setShippingMethod(orderDTO.getShippingMethod().trim());
    //     }

    //     if (orderDTO.getShippingAddress() != null && !orderDTO.getShippingAddress().trim().isEmpty()) {
    //         order.setShippingAddress(orderDTO.getShippingAddress().trim());
    //     }

    //     if (orderDTO.getShippingDate() != null) {
    //         order.setShippingDate(orderDTO.getShippingDate());
    //     }

    //     if (orderDTO.getPaymentMethod() != null && !orderDTO.getPaymentMethod().trim().isEmpty()) {
    //         order.setPaymentMethod(orderDTO.getPaymentMethod().trim());
    //     }

    //     order.setUser(existingUser);
    //     return orderRepository.save(order);
    // }

    // @Override
    // @Transactional
    // public void deleteOrder(Long orderId) {
    //     Order order = getOrderById(orderId);
    //     //no hard-delete, => please soft-delete
    //     if(order != null) {
    //         order.setActive(false);
    //         orderRepository.save(order);
    //     }
    // }
    // @Override
    // public List<OrderResponse> findByUserId(Long userId) {
    //     List<Order> orders = orderRepository.findByUserId(userId);
    //     return orders.stream().map(order -> OrderResponse.fromOrder(order)).toList();
    // }

    // @Override
    // public Page<Order> getOrdersByKeyword(String keyword, Pageable pageable) {
    //     return orderRepository.findByKeyword(keyword, pageable);
    // }
    // @Override
    // @Transactional
    // public Order updateOrderStatus(Long id, String status) throws ResourceNotFoundException, IllegalArgumentException {
    //     // Tìm đơn hàng theo ID
    //     Order order = getOrderById(id); // Sẽ tìm theo ID trước, sau đó tìm theo vnpTxnRef

    //     // Kiểm tra trạng thái hợp lệ
    //     if (status == null || status.trim().isEmpty()) {
    //         throw new IllegalArgumentException("Status cannot be null or empty");
    //     }

    //     // Kiểm tra xem trạng thái có nằm trong danh sách hợp lệ không
    //     if (!OrderStatus.VALID_STATUSES.contains(status)) {
    //         throw new IllegalArgumentException("Invalid status: " + status);
    //     }

    //     // Kiểm tra logic chuyển đổi trạng thái
    //     String currentStatus = order.getStatus();
    //     if (currentStatus.equals(OrderStatus.DELIVERED) && !status.equals(OrderStatus.CANCELLED)) {
    //         throw new IllegalArgumentException("Cannot change status from DELIVERED to " + status);
    //     }

    //     if (currentStatus.equals(OrderStatus.CANCELLED)) {
    //         throw new IllegalArgumentException("Cannot change status of a CANCELLED order");
    //     }

    //     if (status.equals(OrderStatus.CANCELLED)) {
    //         // Kiểm tra xem đơn hàng có thể bị hủy không
    //         if (!currentStatus.equals(OrderStatus.PENDING)) {
    //             throw new IllegalArgumentException("Order can only be cancelled from PENDING status");
    //         }
    //     }

    //     // Cập nhật trạng thái đơn hàng
    //     order.setStatus(status);

    //     // Lưu đơn hàng đã cập nhật
    //     return orderRepository.save(order);
    // }
    @Override
    public OrderResponse getOrderById(Long orderId) throws ResourceNotFoundException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find order with id: " + orderId));
        return modelMapper.map(order, OrderResponse.class);
    }

    @Override
    public List<OrderResponse> findByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream().map(order -> modelMapper.map(order, OrderResponse.class)).toList();
    }

    

    @Override
    public OrderResponse updateOrder(Long id, OrderDTO orderDTO) throws ResourceNotFoundException {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find order with id: " + id));
        // Update the order fields using the DTO
        modelMapper.map(orderDTO, order);
        // Save the updated order
        Order updatedOrder = orderRepository.save(order);
        return modelMapper.map(updatedOrder, OrderResponse.class);
    }

    @Override    public void deleteOrder(Long orderId) throws ResourceNotFoundException{
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find order with id: " + orderId));
        // Set status to cancelled for soft delete
        order.setStatus(OrderStatus.CANCELLED_BY_ADMIN);
        orderRepository.save(order);
    }
}
    
