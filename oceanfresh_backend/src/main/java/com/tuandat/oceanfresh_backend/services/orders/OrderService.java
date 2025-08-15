package com.tuandat.oceanfresh_backend.services.orders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tuandat.oceanfresh_backend.dtos.OrderDTO;
import com.tuandat.oceanfresh_backend.dtos.OrderItemDTO;
import com.tuandat.oceanfresh_backend.dtos.OrderUpdateDTO;
import com.tuandat.oceanfresh_backend.exceptions.DataNotFoundException;
import com.tuandat.oceanfresh_backend.exceptions.InsufficientStockException;
import com.tuandat.oceanfresh_backend.exceptions.InvalidOrderStateException;
import com.tuandat.oceanfresh_backend.exceptions.ResourceNotFoundException;
import com.tuandat.oceanfresh_backend.models.Order;
import com.tuandat.oceanfresh_backend.models.OrderDetail;
import com.tuandat.oceanfresh_backend.models.OrderStatus;
import com.tuandat.oceanfresh_backend.models.PaymentStatus;
import com.tuandat.oceanfresh_backend.models.ProductVariant;
import com.tuandat.oceanfresh_backend.models.User;
import com.tuandat.oceanfresh_backend.repositories.CouponRepository;
import com.tuandat.oceanfresh_backend.repositories.OrderDetailRepository;
import com.tuandat.oceanfresh_backend.repositories.OrderRepository;
import com.tuandat.oceanfresh_backend.repositories.ProductVariantRepository;
import com.tuandat.oceanfresh_backend.repositories.UserRepository;
import com.tuandat.oceanfresh_backend.responses.orders.OrderResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService implements IOrderService {
        private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

        private final OrderRepository orderRepository;
        private final OrderDetailRepository orderDetailRepository;
        private final UserRepository userRepository;
        private final ProductVariantRepository productVariantRepository;
        private final CouponRepository couponRepository;

        // Các chuyển trạng thái hợp lệ

        private static final Map<String, Set<String>> VALID_STATUS_TRANSITIONS = Map.of(
                        "PENDING",
                        Set.of("PROCESSING", "CANCELLED_BY_CUSTOMER",
                                        "CANCELLED_BY_ADMIN"),
                        "PROCESSING", Set.of("SHIPPING", "CANCELLED_BY_ADMIN", "CANCELLED_BY_CUSTOMER"),
                        "SHIPPING", Set.of("DELIVERED"),
                        "DELIVERED", Set.of("RETURNED", "COMFIRMED"),
                        "CANCELLED_BY_CUSTOMER", Set.of(),
                        "CANCELLED_BY_ADMIN", Set.of(),
                        "RETURNED", Set.of());

        @Override
        @Transactional
        public OrderResponse createOrder(OrderDTO orderDTO) throws Exception {
                // 1. Xác thực và lấy thông tin người dùng (có thể null cho khách vãng lai)
                User user = userRepository.findById(orderDTO.getUserId())
                                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id",
                                                orderDTO.getUserId()));
                // 2. Xác thực sản phẩm trong đơn hàng và tính tổng tiền
                List<OrderDetail> orderDetails = validateAndCreateOrderDetails(orderDTO.getOrderItems());
                BigDecimal subtotalAmount = calculateSubtotal(orderDetails);

                // 3. Áp dụng mã giảm giá nếu có (chưa sử dụng)
                // Coupon coupon = null;
                // BigDecimal discountAmount = BigDecimal.ZERO;
                // if (StringUtils.hasText(orderDTO.getCouponCode())) {
                //         coupon = validateAndApplyCoupon(orderDTO.getCouponCode(), subtotalAmount, user);
                //         discountAmount = calculateDiscount(coupon, subtotalAmount);
                // }

                // 4. Tính phí vận chuyển (Chưa sử dụng)
                // BigDecimal shippingFee = calculateShippingFee(orderDTO.getShippingAddress(),
                //                 orderDTO.getShippingMethod());

                // 5. Tính tổng tiền cuối cùng
                // BigDecimal totalAmount = subtotalAmount.add(shippingFee).subtract(discountAmount);

                // 6. Tạo và lưu đơn hàng
                Order order = Order.builder()
                                .orderCode(generateOrderCode())
                                .user(user)
                                .fullName(orderDTO.getFullName())
                                .email(orderDTO.getEmail())
                                .phoneNumber(orderDTO.getPhoneNumber())
                                .shippingAddress(orderDTO.getShippingAddress()).note(orderDTO.getNote())
                                .orderDate(ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDateTime())
                                .status(OrderStatus.PENDING)
                                .subtotalAmount(subtotalAmount)
                                // .shippingFee(shippingFee)
                                // .discountAmount(discountAmount)
                                .totalAmount(subtotalAmount)
                                .shippingMethod(StringUtils.hasText(orderDTO.getShippingMethod())
                                                ? orderDTO.getShippingMethod()
                                                : "Hỏa tốc")
                                .shippingDateExpected(orderDTO.getShippingDateExpected() != null
                                                ? orderDTO.getShippingDateExpected()
                                                : LocalDate.now().plusDays(2))
                                .paymentMethod(orderDTO.getPaymentMethod())
                                .paymentStatus(PaymentStatus.UNPAID)
                                // .coupon(coupon)
                                .vnpTxnRef(orderDTO.getVnpTxnRef())
                                .build();

                // 7. Lưu đơn hàng trước để có ID
                Order savedOrder = orderRepository.save(order);

                // 8. Gán tham chiếu đơn hàng cho chi tiết và lưu
                orderDetails.forEach(detail -> detail.setOrder(savedOrder));
                List<OrderDetail> savedOrderDetails = orderDetailRepository.saveAll(orderDetails);

                // 9. Cập nhật tồn kho sản phẩm (giữ hàng)
                updateInventory(orderDetails, false); // false = giảm tồn kho

                // 10. Cập nhật số lần sử dụng mã giảm giá nếu có
                // if (coupon != null) {
                //         updateCouponUsage(coupon);
                // }

                return buildOrderResponse(savedOrder, savedOrderDetails);
        }

        @Override
        @Transactional(readOnly = true)
        public Order getOrderById(Long orderId) throws ResourceNotFoundException {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", "id", orderId));
                return order;
        }

        @Override
        @Transactional(readOnly = true)
        public OrderResponse getOrderByIdWithDetails(Long orderId) throws ResourceNotFoundException {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", "id", orderId));

                List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
                return buildOrderResponse(order, orderDetails);
        }

        @Override
        @Transactional(readOnly = true)
        public OrderResponse getOrderByCode(String orderCode) throws ResourceNotFoundException {
                Order order = orderRepository.findByOrderCode(orderCode)
                                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", "mã", orderCode));

                List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
                return buildOrderResponse(order, orderDetails);
        }

        @Override
        @Transactional(readOnly = true)
        public List<OrderResponse> findByUserId(Long userId) {
                List<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(userId);
                return orders.stream()
                                .map(order -> {
                                        List<OrderDetail> orderDetails = orderDetailRepository
                                                        .findByOrderId(order.getId());
                                        return buildOrderResponse(order, orderDetails);
                                })
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public Page<OrderResponse> getAllOrders(Pageable pageable) {
                Page<Order> orders = orderRepository.findAll(pageable);
                return orders.map(order -> {
                        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
                        return buildOrderResponse(order, orderDetails);
                });
        }

        @Override
        @Transactional
        public OrderResponse cancelOrder(Long orderId, String reason) throws Exception {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", "id", orderId));

                // Lấy thông tin người dùng hiện tại
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                boolean isAdmin = authentication.getAuthorities().stream()
                                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
                try {
                        if (isAdmin) {
                                String currentStatus = order.getStatus().toString();
                                validateStatusTransition(currentStatus, OrderStatus.CANCELLED_BY_ADMIN.toString());
                                order.setStatus(OrderStatus.CANCELLED_BY_ADMIN);
                                
                                order.setNote(order.getNote() != null
                                                ? order.getNote() + " | Đã hủy bởi admin: " + reason
                                                : "Đã hủy bởi admin: " + reason);
                                handleStatusChange(order, currentStatus, OrderStatus.CANCELLED_BY_ADMIN.toString());
                        } else {
                                // Kiểm tra trạng thái thanh toán - khách hàng chỉ được hủy đơn chưa thanh toán
                                if (PaymentStatus.PAID.equals(order.getPaymentStatus())) {
                                        throw new InvalidOrderStateException(
                                                        "Không thể hủy đơn hàng đã thanh toán. Vui lòng liên hệ hỗ trợ để được trợ giúp.");
                                }
                                
                                String currentStatus = order.getStatus().toString();
                                validateStatusTransition(currentStatus, OrderStatus.CANCELLED_BY_CUSTOMER.toString());
                                order.setStatus(OrderStatus.CANCELLED_BY_CUSTOMER);
                                order.setNote(order.getNote() != null
                                                ? order.getNote() + " | Đã hủy bởi khách hàng: " + reason
                                                : "Đã hủy bởi khách hàng: " + reason);
                                handleStatusChange(order, currentStatus, OrderStatus.CANCELLED_BY_CUSTOMER.toString());
                        }
                        // Kiểm tra trạng thái đơn hàng hiện tại
                        Order updatedOrder = orderRepository.save(order);

                        // Trả về OrderResponse thay vì Order để tránh circular reference
                        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(updatedOrder.getId());
                        return buildOrderResponse(updatedOrder, orderDetails);
                } catch (InvalidOrderStateException e) {
                        logger.error("Lỗi khi hủy đơn hàng {}: {}", orderId, e.getMessage());
                        throw e;
                }
        }

        @Override
        @Transactional(readOnly = true)
        public Page<Order> getOrdersByKeyword(String keyword, Pageable pageable) {
                if (keyword == null || keyword.trim().isEmpty()) {
                        return orderRepository.findAll(pageable);
                }

                String searchKeyword = "%" + keyword.trim().toLowerCase() + "%";
                return orderRepository.findByKeyword(searchKeyword, pageable);
        }

        //
        @Override
        @Transactional
        public OrderResponse updateOrderStatus(Long id, String status) throws DataNotFoundException {
                try {
                        Order order = getOrderById(id);

                        if (order == null) {
                                throw new DataNotFoundException("Không tìm thấy đơn hàng với ID/Code: " + id);
                        }

                        // Kiểm tra quyền của user hiện tại
                        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                        boolean isAdmin = authentication.getAuthorities().stream()
                                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

                        // Nếu không phải admin, kiểm tra trạng thái được phép cập nhật
                        if (!isAdmin) {
                                String upperStatus = status.toUpperCase();
                                if (!isAllowedUserStatus(upperStatus)) {
                                        throw new InvalidOrderStateException(
                                                        "Bạn không có quyền cập nhật trạng thái này: " + upperStatus);
                                }
                                
                                // Kiểm tra thêm: nếu user muốn hủy đơn hàng, kiểm tra trạng thái thanh toán
                                if ("CANCELLED_BY_CUSTOMER".equals(upperStatus)) {
                                        if (PaymentStatus.PAID.equals(order.getPaymentStatus())) {
                                                throw new InvalidOrderStateException(
                                                                "Không thể hủy đơn hàng đã thanh toán. Vui lòng liên hệ hỗ trợ để được trợ giúp.");
                                        }
                                }
                        }

                        String currentStatus = order.getStatus().toString();
                        validateStatusTransition(currentStatus, status);
                        handleStatusChange(order, currentStatus, status);

                        order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
                        Order updatedOrder = orderRepository.save(order);

                        // Trả về OrderResponse thay vì Order để tránh circular reference
                        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(updatedOrder.getId());
                        return buildOrderResponse(updatedOrder, orderDetails);

                } catch (DataNotFoundException e) {
                        throw e;
                } catch (Exception e) {
                        throw new RuntimeException("Lỗi cập nhật trạng thái đơn hàng: " + e.getMessage());
                }
        }

        // Helper method để kiểm tra trạng thái được phép cho user
        private boolean isAllowedUserStatus(String status) {
                return status.equals("PROCESSING") || 
                       status.equals("CANCELLED_BY_CUSTOMER") || 
                       status.equals("CONFIRMED");
                       
        }

        @Override
        @Transactional
        public OrderResponse updateOrderStatusAndPaymentStatusByVnPayOrderId(String vnpTxnRef, String orderStatus,
                        String paymentStatus) throws DataNotFoundException {
                try {
                        Order order = orderRepository.findByVnpTxnRef(vnpTxnRef)
                                        .orElseThrow(() -> new DataNotFoundException(
                                                        "Không tìm thấy đơn hàng với VNPAY Transaction Reference: "
                                                                        + vnpTxnRef));

                        String currentStatus = order.getStatus().toString();
                        validateStatusTransition(currentStatus, orderStatus);
                        handleStatusChange(order, currentStatus, orderStatus);

                        order.setStatus(OrderStatus.valueOf(orderStatus.toUpperCase()));

                        String currentPaymentStatus = order.getPaymentStatus().toString();
                        if (currentPaymentStatus.equals(PaymentStatus.PAID.toString())
                                        && !paymentStatus.equals(PaymentStatus.PAID.toString())) {
                                throw new InvalidOrderStateException(
                                                "Không thể chuyển trạng thái thanh toán từ PAID sang " + paymentStatus);
                        }
                        // Kiểm tra trang thái thanh toán có đúng với ENum không
                        try {
                                PaymentStatus.valueOf(paymentStatus.toUpperCase());
                        } catch (IllegalArgumentException e) {
                                throw new InvalidOrderStateException(
                                                "Trạng thái thanh toán không hợp lệ: " + paymentStatus);
                        }
                        order.setPaymentStatus(PaymentStatus.valueOf(paymentStatus.toUpperCase()));
                        Order updatedOrder = orderRepository.save(order);
                        // Trả về OrderResponse thay vì Order để tránh circular reference
                        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(updatedOrder.getId());
                        return buildOrderResponse(updatedOrder, orderDetails);

                } catch (DataNotFoundException e) {
                        throw e;
                } catch (Exception e) {
                        logger.error("Lỗi khi cập nhật trạng thái thanh toán {}: {}", vnpTxnRef, e.getMessage());
                        throw new RuntimeException("Lỗi cập nhật trạng thái thanh toán: " + e.getMessage());
                }
        }

        @Override
        @Transactional
        public Order updateOrder(Long id, OrderDTO orderDTO) throws DataNotFoundException {
                try {
                        Order order = orderRepository.findById(id)
                                        .orElseThrow(() -> new DataNotFoundException(
                                                        "Không tìm thấy đơn hàng với ID: " + id));

                        // Chỉ cho phép cập nhật một số trường nhất định khi đơn hàng ở trạng thái
                        // PENDING
                        if (!OrderStatus.PENDING.equals(order.getStatus()) &&
                                        !OrderStatus.PROCESSING.equals(order.getStatus())) {
                                throw new InvalidOrderStateException(
                                                "Chỉ có thể cập nhật đơn hàng ở trạng thái Chờ xử lý hoặc Đang xử lý");
                        }

                        // Validate user exists if userId is provided
                        if (orderDTO.getUserId() != null) {
                                User user = userRepository.findById(orderDTO.getUserId())
                                                .orElseThrow(() -> new DataNotFoundException(
                                                                "Không tìm thấy người dùng với ID: "
                                                                                + orderDTO.getUserId()));
                                order.setUser(user);
                        }

                        // Update customer information
                        if (StringUtils.hasText(orderDTO.getFullName())) {
                                order.setFullName(orderDTO.getFullName().trim());
                        }

                        if (StringUtils.hasText(orderDTO.getEmail())) {
                                order.setEmail(orderDTO.getEmail().trim());
                        }

                        if (StringUtils.hasText(orderDTO.getPhoneNumber())) {
                                order.setPhoneNumber(orderDTO.getPhoneNumber().trim());
                        }

                        // Câp nhật địa chỉ giao hàng
                        if (StringUtils.hasText(orderDTO.getShippingAddress())) {
                                order.setShippingAddress(orderDTO.getShippingAddress().trim());
                                // Tính tiền ship đang để ship bằng 0 vì chưa tích hợp đơn vị vân chuyển
                                // BigDecimal newShippingFee = calculateShippingFee(
                                //                 orderDTO.getShippingAddress(), order.getShippingMethod());
                                // order.setShippingFee(newShippingFee);
                        }

                        if (StringUtils.hasText(orderDTO.getShippingMethod())) {
                                order.setShippingMethod(orderDTO.getShippingMethod().trim());
                                // Tính tiền ship đang để ship bằng 0 vì chưa tích hợp đơn vị vân chuyển
                                // BigDecimal newShippingFee = calculateShippingFee(
                                //                 order.getShippingAddress(), orderDTO.getShippingMethod());
                                // order.setShippingFee(newShippingFee);
                        }
                        // Cập nhật ngày giao hàng dự kiến nếu có
                        if (orderDTO.getShippingDateExpected() != null) {
                                order.setShippingDateExpected(orderDTO.getShippingDateExpected());
                        }

                        // Cap nhật phương thức thanh toán nếu có
                        if (StringUtils.hasText(orderDTO.getPaymentMethod())) {
                                order.setPaymentMethod(orderDTO.getPaymentMethod().trim());
                        }

                        // Câp nhật lại ghi chú của khách nếu có
                        if (orderDTO.getNote() != null) {
                                order.setNote(orderDTO.getNote().trim());
                        }

                        // TÍnh toán lại tổng tiền
                        BigDecimal newTotal = order.getSubtotalAmount();
                                        // .add(order.getShippingFee())
                                        // .subtract(order.getDiscountAmount());
                        order.setTotalAmount(newTotal);

                        Order updatedOrder = orderRepository.save(order);

                        return updatedOrder;

                } catch (DataNotFoundException e) {
                        throw e;
                } catch (Exception e) {
                        logger.error("Lỗi khi cập nhật đơn hàng {}: {}", id, e.getMessage());
                        throw new RuntimeException("Lỗi cập nhật đơn hàng: " + e.getMessage());
                }
        }

        @Override
        @Transactional(readOnly = true)
        public List<OrderResponse> getOrdersByUserId(Long userId) {
                try {
                        // Validate user exists
                        userRepository.findById(userId)
                                        .orElseThrow(() -> new DataNotFoundException(
                                                        "Không tìm thấy người dùng với ID: " + userId));

                        List<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(userId);
                        return orders.stream()
                                        .map(order -> {
                                                List<OrderDetail> orderDetails = orderDetailRepository
                                                                .findByOrderId(order.getId());
                                                return buildOrderResponse(order, orderDetails);
                                        })
                                        .collect(Collectors.toList());

                } catch (Exception e) {
                        logger.error("Lỗi khi lấy danh sách đơn hàng của user {}: {}", userId, e.getMessage());
                        throw new RuntimeException("Lỗi lấy danh sách đơn hàng: " + e.getMessage());
                }
        }

        // Check xem đơn hàng được hủy bởi người dùng hay admin
        @Override
        @Transactional
        public void deleteOrder(Long orderId) {
                Order order = getOrderById(orderId);
                // no hard-delete, => please soft-delete
                if (order != null) {
                        order.setActive(false);
                        orderRepository.save(order);
                }
        }

        // ===== CÁC PHƯƠNG THỨC HỖ TRỢ =====

        private List<OrderDetail> validateAndCreateOrderDetails(List<OrderItemDTO> orderItems) {
                List<OrderDetail> orderDetails = new ArrayList<>();

                for (OrderItemDTO item : orderItems) {
                        ProductVariant variant = productVariantRepository.findById(item.getProductVariantId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Biến thể sản phẩm", "id",
                                                        item.getProductVariantId()));

                        // Xác thực biến thể có hoạt động không
                        if (!variant.isActive()) {
                                throw new InvalidOrderStateException(
                                                "Biến thể sản phẩm " + variant.getSku() + " hiện không khả dụng");
                        }

                        // Xác thực tồn kho
                        if (variant.getQuantityInStock() < item.getQuantity()) {
                                throw new InsufficientStockException("Không đủ hàng cho sản phẩm " + variant.getSku() +
                                                ". Còn lại: " + variant.getQuantityInStock() + ", Yêu cầu: "
                                                + item.getQuantity());
                        } // Luôn sử dụng giá hiện tại từ database để đảm bảo tính bảo mật
                          // Không chấp nhận giá từ client để tránh thao túng giá
                        BigDecimal priceAtOrder = variant.getPrice();
                        BigDecimal lineTotal = priceAtOrder.multiply(BigDecimal.valueOf(item.getQuantity()));

                        OrderDetail detail = OrderDetail.builder()
                                        .productVariant(variant)
                                        .quantity(item.getQuantity())
                                        .priceAtOrder(priceAtOrder)
                                        .totalLineAmount(lineTotal)
                                        .build();

                        orderDetails.add(detail);
                }

                return orderDetails;
        }

        private BigDecimal calculateSubtotal(List<OrderDetail> orderDetails) {
                return orderDetails.stream()
                                .map(OrderDetail::getTotalLineAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }


        private void validateStatusTransition(String currentStatus, String newStatus) {
                Set<String> validTransitions = VALID_STATUS_TRANSITIONS.get(currentStatus);

                if (validTransitions == null || !validTransitions.contains(newStatus)) {
                        throw new InvalidOrderStateException(
                                        "Không thể chuyển trạng thái từ " + currentStatus + " sang " + newStatus);
                }
        }

        // Xử lý thay đổi trạng thái đơn hàng
        private void handleStatusChange(Order order, String oldStatus, String newStatus) {
                switch (newStatus) {
                        case "PROCESSING":
                                
                                break;
                        case "SHIPPING":
                                order.setActualShippingDate(
                                                ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDateTime());
                                order.setTrackingNumber(generateTrackingNumber());
                                break;

                        case "DELIVERED":
                                // Đánh dấu đã thanh toán cho COD
                                if ("COD".equalsIgnoreCase(order.getPaymentMethod()) &&
                                                PaymentStatus.UNPAID.equals(order.getPaymentStatus())) {
                                        order.setPaymentStatus(PaymentStatus.PAID);
                                }
                                break;

                        case "CANCELLED_BY_ADMIN":
                        case "CANCELLED_BY_CUSTOMER":
                                // Hoàn trả tồn kho nếu chưa hoàn trả
                                if (!"CANCELLED_BY_ADMIN".equals(oldStatus) &&
                                                !"CANCELLED_BY_CUSTOMER".equals(oldStatus)) {
                                        List<OrderDetail> orderDetails = orderDetailRepository
                                                        .findByOrderId(order.getId());
                                        updateInventory(orderDetails, true);
                                }
                                break;

                        case "RETURNED":
                                // Xử lý logic trả hàng - hoàn trả tồn kho
                                List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
                                updateInventory(orderDetails, true);
                                order.setPaymentStatus(PaymentStatus.REFUNDED);
                                break;
                }
        }

        // check xem người dùng có phải là chủ sở hữu của đơn hàng không
        public boolean isOrderOwner(Long orderId, Long userId) {
                try {
                        Order order = orderRepository.findById(orderId)
                                        .orElse(null);

                        if (order == null) {
                                return false;
                        }

                        return order.getUser() != null && order.getUser().getId().equals(userId);
                } catch (Exception e) {
                        logger.error("Error checking order ownership for orderId: {} and userId: {}", orderId, userId,
                                        e);
                        return false;
                }
        }

        private String generateTrackingNumber() {
                return "TRK" + System.currentTimeMillis();
        }

        private OrderResponse buildOrderResponse(Order order, List<OrderDetail> orderDetails) {
                // Set orderDetails vào order để fromOrder có thể truy cập
                order.setOrderDetails(orderDetails);

                return OrderResponse.fromOrder(order);
        }

        private String generateOrderCode() {
                String timestamp = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                                .toLocalDateTime()
                                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                return "OF" + timestamp;
        }

        // Cập nhật tồn kho
        private void updateInventory(List<OrderDetail> orderDetails, boolean restore) {
                for (OrderDetail detail : orderDetails) {
                        ProductVariant variant = detail.getProductVariant();
                        int adjustment = restore ? detail.getQuantity() : -detail.getQuantity();
                        int newStock = variant.getQuantityInStock() + adjustment;

                        if (!restore && newStock < 0) {
                                throw new InsufficientStockException(
                                                "Không đủ tồn kho cho biến thể " + variant.getSku());
                        }

                        variant.setQuantityInStock(newStock);
                        productVariantRepository.save(variant);

                        logger.debug("Cập nhật tồn kho cho biến thể {}: {} -> {}",
                                        variant.getSku(), variant.getQuantityInStock() - adjustment, newStock);
                }
        }

        @Override
        public OrderResponse updateOrderByAdmin(Long orderId, OrderUpdateDTO orderUpdateDTO) throws ResourceNotFoundException {
                try {
                        Order order = getOrderById(orderId);

                        if (order == null) {
                                throw new DataNotFoundException("Không tìm thấy đơn hàng với ID/Code: " + orderId);
                        }

                        // Kiểm tra quyền của user hiện tại
                        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                        boolean isAdmin = authentication.getAuthorities().stream()
                                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

                        // Nếu không phải admin, kiểm tra trạng thái được phép cập nhật
                        if (!isAdmin) {
                                String upperStatus = orderUpdateDTO.getStatus().toUpperCase();
                                if (!isAllowedUserStatus(upperStatus)) {
                                        throw new InvalidOrderStateException(
                                                        "Bạn không có quyền cập nhật trạng thái này: " + upperStatus);
                                }
                        }
                        order.setShippingMethod(orderUpdateDTO.getShippingMethod());
                        order.setShippingAddress(orderUpdateDTO.getShippingAddress());
                        order.setNote(orderUpdateDTO.getNote());
                        order.setShippingDateExpected(orderUpdateDTO.getShippingDateExpected());
                        order.setStatus(OrderStatus.valueOf(orderUpdateDTO.getStatus().toUpperCase()));
                        Order updatedOrder = orderRepository.save(order);

                        // Trả về OrderResponse thay vì Order để tránh circular reference
                        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(updatedOrder.getId());
                        return buildOrderResponse(updatedOrder, orderDetails);

                } catch (ResourceNotFoundException e) {
                        throw e;
                } catch (Exception e) {
                        throw new RuntimeException("Lỗi cập nhật trạng thái đơn hàng: " + e.getMessage());
                }
        }
        // private Coupon validateAndApplyCoupon(String couponCode, BigDecimal subtotalAmount, User user)
        //                 throws ResourceNotFoundException, InvalidOrderStateException {

        //         Coupon coupon = couponRepository.findByCode(couponCode)
        //                         .orElseThrow(() -> new ResourceNotFoundException("Mã giảm giá", "code", couponCode));

        //         // Check if coupon is active
        //         if (!coupon.getIsActive()) {
        //                 throw new InvalidOrderStateException("Mã giảm giá không còn hiệu lực");
        //         }

        //         LocalDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDateTime();

        //         // Kiểm tra thời gian hiệu lực
        //         if (now.isBefore(coupon.getStartDate()) || now.isAfter(coupon.getEndDate())) {
        //                 throw new InvalidOrderStateException("Mã giảm giá đã hết hạn hoặc chưa có hiệu lực");
        //         }

        //         // Kiểm tra giá trị đơn hàng tối thiểu
        //         if (subtotalAmount.compareTo(coupon.getMinimumOrderValue()) < 0) {
        //                 throw new InvalidOrderStateException(
        //                                 String.format("Đơn hàng phải có giá trị tối thiểu %s để sử dụng mã giảm giá này",
        //                                                 coupon.getMinimumOrderValue()));
        //         }

        //         // Kiểm tra số lần sử dụng tối đa
        //         if (coupon.getMaxTotalUsage() != null &&
        //                         coupon.getCurrentUsageCount() >= coupon.getMaxTotalUsage()) {
        //                 throw new InvalidOrderStateException("Mã giảm giá đã hết lượt sử dụng");
        //         }

        //         return coupon;
        // }

        // private BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotalAmount) {
        //         if (DiscountType.percentage.equals(coupon.getDiscountType())) {
        //                 BigDecimal discountAmount = subtotalAmount.multiply(coupon.getDiscountValue())
        //                                 .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        //                 if (coupon.getMaxDiscountAmount() != null) {
        //                         return discountAmount.min(coupon.getMaxDiscountAmount());
        //                 }
        //                 return discountAmount;
        //         } else if (DiscountType.fixed_amount.equals(coupon.getDiscountType())) {
        //                 return coupon.getDiscountValue().min(subtotalAmount);
        //         }

        //         return BigDecimal.ZERO;
        // }

        // private BigDecimal calculateShippingFee(String address, String shippingMethod) {
        //         // Không tính phí vận chuyển
        //         return BigDecimal.ZERO;
        // }

        

        // Cập nhật số lần sử dụng mã giảm giá
        // Chưa dùng tới
        // private void updateCouponUsage(Coupon coupon) {
        //         coupon.setCurrentUsageCount(coupon.getCurrentUsageCount() + 1);

        //         // Vô hiệu hóa nếu đã đạt giới hạn sử dụng
        //         if (coupon.getMaxTotalUsage() != null && coupon.getCurrentUsageCount() >= coupon.getMaxTotalUsage()) {
        //                 coupon.setIsActive(false);
        //                 logger.info("Mã giảm giá {} đã bị vô hiệu hóa do đạt giới hạn sử dụng", coupon.getCode());
        //         }

        //         couponRepository.save(coupon);
        // }

        

}
