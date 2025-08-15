package com.tuandat.oceanfresh_backend.services.cart;

import com.tuandat.oceanfresh_backend.dtos.cart.AddToCartDTO;
import com.tuandat.oceanfresh_backend.dtos.cart.RemoveFromCartDTO;
import com.tuandat.oceanfresh_backend.dtos.cart.UpdateCartItemDTO;
import com.tuandat.oceanfresh_backend.responses.cart.CartResponse;
import com.tuandat.oceanfresh_backend.responses.cart.CartSummaryResponse;

public interface ICartService {

    // Thêm sản phẩm vào giỏ hàng
    CartResponse addToCart(AddToCartDTO addToCartDTO) throws Exception;

    // Cập nhật số lượng sản phẩm trong giỏ hàng
    CartResponse updateCartItem(UpdateCartItemDTO updateCartItemDTO) throws Exception;

    // Xóa sản phẩm khỏi giỏ hàng
    CartResponse removeFromCart(RemoveFromCartDTO removeFromCartDTO) throws Exception;

    // Lấy thông tin giỏ hàng đầy đủ
    CartResponse getCart(Long userId, String sessionId) throws Exception;

    // Lấy tóm tắt giỏ hàng (số lượng, tổng tiền)
    CartSummaryResponse getCartSummary(Long userId, String sessionId) throws Exception;

    // Xóa toàn bộ giỏ hàng
    void clearCart(Long userId, String sessionId) throws Exception;

    // Chuyển giỏ hàng từ session sang user khi đăng nhập
    CartResponse mergeCart(Long userId, String sessionId) throws Exception;

    // Xóa cart item theo ID
    CartResponse removeCartItem(Long cartItemId, Long userId, String sessionId) throws Exception;

    // Tăng số lượng sản phẩm trong giỏ hàng
    CartResponse increaseQuantity(Long productVariantId, Long userId, String sessionId) throws Exception;

    // Giảm số lượng sản phẩm trong giỏ hàng
    CartResponse decreaseQuantity(Long productVariantId, Long userId, String sessionId) throws Exception;

    // Kiểm tra tính khả dụng của các sản phẩm trong giỏ hàng
    CartResponse validateCartItems(Long userId, String sessionId) throws Exception;
}
