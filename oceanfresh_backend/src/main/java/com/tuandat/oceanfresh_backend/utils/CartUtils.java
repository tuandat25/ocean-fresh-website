package com.tuandat.oceanfresh_backend.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Utility class cho các thao tác liên quan đến giỏ hàng
 */
public class CartUtils {

    // Phí vận chuyển mặc định (30k VND)
    public static final BigDecimal DEFAULT_SHIPPING_FEE = BigDecimal.valueOf(30000);
    
    // Giá trị đơn hàng miễn phí vận chuyển (500k VND)
    public static final BigDecimal FREE_SHIPPING_THRESHOLD = BigDecimal.valueOf(500000);
    
    // Số lượng tối đa mỗi sản phẩm trong giỏ hàng
    public static final int MAX_QUANTITY_PER_ITEM = 99;
    
    // Số lượng tối đa sản phẩm khác nhau trong giỏ hàng
    public static final int MAX_ITEMS_IN_CART = 50;

    /**
     * Tạo session ID mới cho khách vãng lai
     */
    public static String generateSessionId() {
        return "sess_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    /**
     * Tính phí vận chuyển dựa trên tổng tiền đơn hàng
     */
    public static BigDecimal calculateShippingFee(BigDecimal subtotal) {
        // Không tính phí vận chuyển
        return BigDecimal.ZERO;
    }

    /**
     * Tính tổng tiền đơn hàng (bao gồm phí vận chuyển)
     */
    public static BigDecimal calculateTotal(BigDecimal subtotal, BigDecimal shippingFee) {
        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (shippingFee == null) shippingFee = BigDecimal.ZERO;
        
        return subtotal.add(shippingFee);
    }

    /**
     * Kiểm tra số lượng có hợp lệ không
     */
    public static boolean isValidQuantity(Integer quantity) {
        return quantity != null && quantity > 0 && quantity <= MAX_QUANTITY_PER_ITEM;
    }

    /**
     * Kiểm tra giỏ hàng có vượt quá giới hạn số lượng sản phẩm không
     */
    public static boolean exceedsMaxItems(int currentItemCount) {
        return currentItemCount >= MAX_ITEMS_IN_CART;
    }

    /**
     * Tạo message thông báo tồn kho
     */
    public static String getStockMessage(int availableStock) {
        if (availableStock <= 0) {
            return "Sản phẩm đã hết hàng";
        } else if (availableStock <= 5) {
            return "Chỉ còn " + availableStock + " sản phẩm";
        } else {
            return "Còn " + availableStock + " sản phẩm";
        }
    }

    /**
     * Kiểm tra xem có đủ tồn kho không
     */
    public static boolean hasEnoughStock(int requestedQuantity, int availableStock) {
        return availableStock >= requestedQuantity;
    }

    /**
     * Tính tỷ lệ phần trăm giảm giá
     */
    public static BigDecimal calculateDiscountPercentage(BigDecimal originalPrice, BigDecimal currentPrice) {
        if (originalPrice == null || currentPrice == null || 
            originalPrice.compareTo(BigDecimal.ZERO) <= 0 || 
            currentPrice.compareTo(originalPrice) >= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = originalPrice.subtract(currentPrice);
        return discount.multiply(BigDecimal.valueOf(100))
                      .divide(originalPrice, 2, RoundingMode.HALF_UP);
    }

    /**
     * Format giá tiền thành chuỗi hiển thị
     */
    public static String formatPrice(BigDecimal price) {
        if (price == null) {
            return "0 ₫";
        }
        
        // Làm tròn về số nguyên và format với dấu phẩy
        long priceAsLong = price.longValue();
        return String.format("%,d ₫", priceAsLong);
    }

    /**
     * Kiểm tra session ID có hợp lệ không
     */
    public static boolean isValidSessionId(String sessionId) {
        return sessionId != null && 
               sessionId.length() > 5 && 
               sessionId.startsWith("sess_");
    }

    /**
     * Tạo message cho việc thêm sản phẩm vào giỏ hàng
     */
    public static String getAddToCartMessage(String productName, int quantity) {
        return String.format("Đã thêm %d x %s vào giỏ hàng", quantity, productName);
    }

    /**
     * Tạo message cho việc cập nhật số lượng sản phẩm
     */
    public static String getUpdateQuantityMessage(String productName, int oldQuantity, int newQuantity) {
        if (newQuantity > oldQuantity) {
            return String.format("Đã tăng số lượng %s từ %d lên %d", productName, oldQuantity, newQuantity);
        } else {
            return String.format("Đã giảm số lượng %s từ %d xuống %d", productName, oldQuantity, newQuantity);
        }
    }

    /**
     * Tạo message cho việc xóa sản phẩm khỏi giỏ hàng
     */
    public static String getRemoveFromCartMessage(String productName) {
        return String.format("Đã xóa %s khỏi giỏ hàng", productName);
    }
}
