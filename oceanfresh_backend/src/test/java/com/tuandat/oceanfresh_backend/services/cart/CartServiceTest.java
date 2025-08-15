package com.tuandat.oceanfresh_backend.services.cart;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.tuandat.oceanfresh_backend.dtos.cart.AddToCartDTO;
import com.tuandat.oceanfresh_backend.dtos.cart.UpdateCartItemDTO;
import com.tuandat.oceanfresh_backend.responses.cart.CartResponse;
import com.tuandat.oceanfresh_backend.responses.cart.CartSummaryResponse;
import com.tuandat.oceanfresh_backend.utils.CartUtils;

/**
 * Unit tests for Cart-related utilities, DTOs, and response objects
 * Tests core functionality that doesn't require database mocking
 */
class CartServiceTest {

    @Test
    void testCartUtils_GenerateSessionId() {
        // Test session ID generation
        String sessionId = CartUtils.generateSessionId();
        
        assertNotNull(sessionId);
        assertTrue(sessionId.startsWith("sess_"));
        assertTrue(sessionId.length() > 5);
        
        // Generate multiple IDs to ensure they're unique
        String sessionId2 = CartUtils.generateSessionId();
        assertNotEquals(sessionId, sessionId2);
    }

    @Test
    void testCartUtils_CalculateShippingFee() {
        // Test shipping fee calculation - should always return zero now
        BigDecimal lowAmount = BigDecimal.valueOf(100000); // 100k
        BigDecimal shippingLow = CartUtils.calculateShippingFee(lowAmount);
        assertEquals(BigDecimal.ZERO, shippingLow);
        
        // Test shipping fee calculation for high amounts - should still be zero
        BigDecimal highAmount = BigDecimal.valueOf(600000); // 600k
        BigDecimal shippingHigh = CartUtils.calculateShippingFee(highAmount);
        assertEquals(BigDecimal.ZERO, shippingHigh);
        
        // Test edge case: exactly at threshold - should be zero
        BigDecimal thresholdAmount = CartUtils.FREE_SHIPPING_THRESHOLD;
        BigDecimal shippingThreshold = CartUtils.calculateShippingFee(thresholdAmount);
        assertEquals(BigDecimal.ZERO, shippingThreshold);
        
        // Test null and zero amounts - should be zero
        assertEquals(BigDecimal.ZERO, CartUtils.calculateShippingFee(null));
        assertEquals(BigDecimal.ZERO, CartUtils.calculateShippingFee(BigDecimal.ZERO));
    }

    @Test
    void testCartUtils_CalculateTotal() {
        // Test total calculation with valid amounts
        BigDecimal subtotal = BigDecimal.valueOf(200000);
        BigDecimal shipping = BigDecimal.valueOf(0); // Shipping is now always 0
        BigDecimal total = CartUtils.calculateTotal(subtotal, shipping);
        assertEquals(BigDecimal.valueOf(200000), total);
        
        // Test with null values
        assertEquals(BigDecimal.ZERO, CartUtils.calculateTotal(null, shipping));
        assertEquals(BigDecimal.valueOf(200000), CartUtils.calculateTotal(subtotal, null));
        assertEquals(BigDecimal.ZERO, CartUtils.calculateTotal(null, null));
    }

    @Test
    void testCartUtils_IsValidQuantity() {
        // Test valid quantities
        assertTrue(CartUtils.isValidQuantity(1));
        assertTrue(CartUtils.isValidQuantity(50));
        assertTrue(CartUtils.isValidQuantity(CartUtils.MAX_QUANTITY_PER_ITEM));
        
        // Test invalid quantities
        assertFalse(CartUtils.isValidQuantity(0));
        assertFalse(CartUtils.isValidQuantity(-1));
        assertFalse(CartUtils.isValidQuantity(null));
        assertFalse(CartUtils.isValidQuantity(CartUtils.MAX_QUANTITY_PER_ITEM + 1));
        assertFalse(CartUtils.isValidQuantity(1000));
    }

    @Test
    void testCartUtils_ExceedsMaxItems() {
        // Test cases around max items limit
        assertFalse(CartUtils.exceedsMaxItems(0));
        assertFalse(CartUtils.exceedsMaxItems(CartUtils.MAX_ITEMS_IN_CART - 1));
        assertTrue(CartUtils.exceedsMaxItems(CartUtils.MAX_ITEMS_IN_CART));
        assertTrue(CartUtils.exceedsMaxItems(CartUtils.MAX_ITEMS_IN_CART + 1));
        assertTrue(CartUtils.exceedsMaxItems(100));
    }

    @Test
    void testCartUtils_GetStockMessage() {
        // Test different stock levels
        assertEquals("Sản phẩm đã hết hàng", CartUtils.getStockMessage(0));
        assertEquals("Chỉ còn 1 sản phẩm", CartUtils.getStockMessage(1));
        assertEquals("Chỉ còn 3 sản phẩm", CartUtils.getStockMessage(3));
        assertEquals("Chỉ còn 5 sản phẩm", CartUtils.getStockMessage(5));
        assertEquals("Còn 10 sản phẩm", CartUtils.getStockMessage(10));
        assertEquals("Còn 100 sản phẩm", CartUtils.getStockMessage(100));
    }

    @Test
    void testCartUtils_HasEnoughStock() {
        // Test stock availability scenarios
        assertTrue(CartUtils.hasEnoughStock(5, 10));
        assertTrue(CartUtils.hasEnoughStock(10, 10));
        assertTrue(CartUtils.hasEnoughStock(0, 5));
        
        assertFalse(CartUtils.hasEnoughStock(15, 10));
        assertFalse(CartUtils.hasEnoughStock(1, 0));
        assertFalse(CartUtils.hasEnoughStock(5, 3));
    }

    @Test
    void testCartUtils_CalculateDiscountPercentage() {
        // Test discount percentage calculation
        BigDecimal originalPrice = BigDecimal.valueOf(100000);
        BigDecimal discountedPrice = BigDecimal.valueOf(80000);
        BigDecimal discountPercentage = CartUtils.calculateDiscountPercentage(originalPrice, discountedPrice);
        assertEquals(BigDecimal.valueOf(20.00), discountPercentage);
        
        // Test 50% discount
        BigDecimal halfPrice = BigDecimal.valueOf(50000);
        BigDecimal halfDiscount = CartUtils.calculateDiscountPercentage(originalPrice, halfPrice);
        assertEquals(BigDecimal.valueOf(50.00), halfDiscount);
        
        // Test invalid cases
        assertEquals(BigDecimal.ZERO, CartUtils.calculateDiscountPercentage(null, discountedPrice));
        assertEquals(BigDecimal.ZERO, CartUtils.calculateDiscountPercentage(originalPrice, null));
        assertEquals(BigDecimal.ZERO, CartUtils.calculateDiscountPercentage(originalPrice, originalPrice));
        assertEquals(BigDecimal.ZERO, CartUtils.calculateDiscountPercentage(originalPrice, BigDecimal.valueOf(150000)));
    }

    @Test
    void testCartUtils_FormatPrice() {
        // Test price formatting
        assertEquals("123,456 ₫", CartUtils.formatPrice(BigDecimal.valueOf(123456)));
        assertEquals("1,000,000 ₫", CartUtils.formatPrice(BigDecimal.valueOf(1000000)));
        assertEquals("0 ₫", CartUtils.formatPrice(BigDecimal.ZERO));
        assertEquals("0 ₫", CartUtils.formatPrice(null));
        
        // Test with decimal values (should round down to long)
        assertEquals("123 ₫", CartUtils.formatPrice(BigDecimal.valueOf(123.99)));
    }

    @Test
    void testCartUtils_IsValidSessionId() {
        // Test valid session IDs
        assertTrue(CartUtils.isValidSessionId("sess_123456789"));
        assertTrue(CartUtils.isValidSessionId("sess_abcdef"));
        
        // Test invalid session IDs
        assertFalse(CartUtils.isValidSessionId("invalid"));
        assertFalse(CartUtils.isValidSessionId("sess_"));
        assertFalse(CartUtils.isValidSessionId("sess"));
        assertFalse(CartUtils.isValidSessionId(""));
        assertFalse(CartUtils.isValidSessionId(null));
        assertFalse(CartUtils.isValidSessionId("prefix_123456"));
    }

    @Test
    void testCartUtils_GetMessages() {
        // Test message generation functions
        String addMessage = CartUtils.getAddToCartMessage("Cà chua", 2);
        assertEquals("Đã thêm 2 x Cà chua vào giỏ hàng", addMessage);
        
        String updateIncreaseMessage = CartUtils.getUpdateQuantityMessage("Cà chua", 2, 5);
        assertEquals("Đã tăng số lượng Cà chua từ 2 lên 5", updateIncreaseMessage);
        
        String updateDecreaseMessage = CartUtils.getUpdateQuantityMessage("Cà chua", 5, 2);
        assertEquals("Đã giảm số lượng Cà chua từ 5 xuống 2", updateDecreaseMessage);
        
        String removeMessage = CartUtils.getRemoveFromCartMessage("Cà chua");
        assertEquals("Đã xóa Cà chua khỏi giỏ hàng", removeMessage);
    }

    @Test
    void testAddToCartDTO_Creation() {
        // Test DTO creation and validation
        AddToCartDTO dto = AddToCartDTO.builder()
                .productVariantId(1L)
                .quantity(3)
                .userId(1L)
                .sessionId("sess_test123")
                .build();
        
        assertEquals(1L, dto.getProductVariantId());
        assertEquals(3, dto.getQuantity());
        assertEquals(1L, dto.getUserId());
        assertEquals("sess_test123", dto.getSessionId());
    }

    @Test
    void testUpdateCartItemDTO_Creation() {
        // Test DTO creation and validation
        UpdateCartItemDTO dto = UpdateCartItemDTO.builder()
                .cartItemId(1L)
                .quantity(5)
                .userId(1L)
                .sessionId("sess_test123")
                .build();
        
        assertEquals(1L, dto.getCartItemId());
        assertEquals(5, dto.getQuantity());
        assertEquals(1L, dto.getUserId());
        assertEquals("sess_test123", dto.getSessionId());
    }

    @Test
    void testCartResponse_Creation() {
        // Test response object creation
        CartResponse response = CartResponse.builder()
                .cartId(1L)
                .userId(1L)
                .totalItems(2)
                .totalQuantity(5)
                .subtotalAmount(BigDecimal.valueOf(200000))
                .estimatedShipping(BigDecimal.ZERO)
                .estimatedTotal(BigDecimal.valueOf(200000))
                .isEmpty(false)
                .build();
        
        assertEquals(1L, response.getCartId());
        assertEquals(1L, response.getUserId());
        assertEquals(2, response.getTotalItems());
        assertEquals(5, response.getTotalQuantity());
        assertEquals(BigDecimal.valueOf(200000), response.getSubtotalAmount());
        assertEquals(BigDecimal.valueOf(30000), response.getEstimatedShipping());
        assertEquals(BigDecimal.valueOf(200000), response.getEstimatedTotal());
        assertFalse(response.getIsEmpty());
    }

    @Test
    void testCartSummaryResponse_Creation() {
        // Test summary response object creation
        CartSummaryResponse response = CartSummaryResponse.builder()
                .totalItems(3)
                .totalQuantity(8)
                .subtotalAmount(BigDecimal.valueOf(300000))
                .estimatedShipping(BigDecimal.ZERO)
                .estimatedTotal(BigDecimal.valueOf(300000))
                .build();
        
        assertEquals(3, response.getTotalItems());
        assertEquals(8, response.getTotalQuantity());
        assertEquals(BigDecimal.valueOf(300000), response.getSubtotalAmount());
        assertEquals(BigDecimal.ZERO, response.getEstimatedShipping());
        assertEquals(BigDecimal.valueOf(300000), response.getEstimatedTotal());
    }

    @Test
    void testCartUtils_Constants() {
        // Test that constants have expected values (even though shipping fee is not used)
        assertEquals(BigDecimal.valueOf(30000), CartUtils.DEFAULT_SHIPPING_FEE);
        assertEquals(BigDecimal.valueOf(500000), CartUtils.FREE_SHIPPING_THRESHOLD);
        assertEquals(99, CartUtils.MAX_QUANTITY_PER_ITEM);
        assertEquals(50, CartUtils.MAX_ITEMS_IN_CART);
    }
}
