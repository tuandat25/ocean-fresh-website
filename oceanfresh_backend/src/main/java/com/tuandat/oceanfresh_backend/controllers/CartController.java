package com.tuandat.oceanfresh_backend.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tuandat.oceanfresh_backend.components.SecurityUtils;
import com.tuandat.oceanfresh_backend.dtos.cart.AddToCartDTO;
import com.tuandat.oceanfresh_backend.dtos.cart.RemoveFromCartDTO;
import com.tuandat.oceanfresh_backend.dtos.cart.UpdateCartItemDTO;
import com.tuandat.oceanfresh_backend.models.User;
import com.tuandat.oceanfresh_backend.responses.ResponseObject;
import com.tuandat.oceanfresh_backend.responses.cart.CartResponse;
import com.tuandat.oceanfresh_backend.responses.cart.CartSummaryResponse;
import com.tuandat.oceanfresh_backend.services.cart.ICartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/cart")
@RequiredArgsConstructor
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);
    private final ICartService cartService;
    private final SecurityUtils securityUtils;

    private boolean hasCartAccess(Long requestedUserId) {
        // Nếu không truyền userId (guest) thì cho phép
        if (requestedUserId == null) {
            return true;
        }
        
        // Lấy thông tin user đang đăng nhập
        User currentUser = securityUtils.getLoggedInUser();
        
        // Nếu chưa đăng nhập nhưng cố tình truyền userId
        if (currentUser == null) {
            return false;
        }
        
        // Nếu là admin thì cho phép truy cập tất cả
        if (currentUser.getRole().getName().equals("ADMIN")) {
            return true;
        }
        
        // User chỉ được truy cập giỏ hàng của chính mình
        return currentUser.getId().equals(requestedUserId);
    }

    @PostMapping("/add")
    // Không cần @PreAuthorize vì cart hỗ trợ cả guest và user
    public ResponseEntity<ResponseObject> addToCart(
            @Valid @RequestBody AddToCartDTO addToCartDTO,
            BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors().stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(ResponseObject.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .message("Validation errors")
                        .data(errors)
                        .build());
            }

            // Kiểm tra quyền truy cập
            if (!hasCartAccess(addToCartDTO.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ResponseObject.builder()
                                .status(HttpStatus.FORBIDDEN)
                                .message("Không có quyền thao tác với giỏ hàng này")
                                .build());
            }

            CartResponse cartResponse = cartService.addToCart(addToCartDTO);
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Thêm sản phẩm vào giỏ hàng thành công")
                    .data(cartResponse)
                    .build());

        } catch (Exception e) {
            logger.error("Lỗi khi thêm sản phẩm vào giỏ hàng: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message("Lỗi khi thêm sản phẩm vào giỏ hàng: " + e.getMessage())
                            .build());
        }
    }

    @PutMapping("/update")
    // Không cần @PreAuthorize vì cart hỗ trợ cả guest và user
    public ResponseEntity<ResponseObject> updateCartItem(
            @Valid @RequestBody UpdateCartItemDTO updateCartItemDTO,
            BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors().stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(ResponseObject.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .message("Validation errors")
                        .data(errors)
                        .build());
            }

            // Kiểm tra quyền truy cập
            if (!hasCartAccess(updateCartItemDTO.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ResponseObject.builder()
                                .status(HttpStatus.FORBIDDEN)
                                .message("Không có quyền thao tác với giỏ hàng này")
                                .build());
            }

            CartResponse cartResponse = cartService.updateCartItem(updateCartItemDTO);
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Cập nhật giỏ hàng thành công")
                    .data(cartResponse)
                    .build());

        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật giỏ hàng: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message("Lỗi khi cập nhật giỏ hàng: " + e.getMessage())
                            .build());
        }
    }

    // API xóa sản phẩm khỏi giỏ hàng
    @DeleteMapping("/remove")
    public ResponseEntity<ResponseObject> removeFromCart(
            @Valid @RequestBody RemoveFromCartDTO removeFromCartDTO,
            BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors().stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(ResponseObject.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .message("Validation errors")
                        .data(errors)
                        .build());
            }

            // Kiểm tra quyền truy cập
            if (!hasCartAccess(removeFromCartDTO.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ResponseObject.builder()
                                .status(HttpStatus.FORBIDDEN)
                                .message("Không có quyền thao tác với giỏ hàng này")
                                .build());
            }

            CartResponse cartResponse = cartService.removeFromCart(removeFromCartDTO);
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Xóa sản phẩm khỏi giỏ hàng thành công")
                    .data(cartResponse)
                    .build());

        } catch (Exception e) {
            logger.error("Lỗi khi xóa sản phẩm khỏi giỏ hàng: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message(e.getMessage())
                            .build());
        }
    }

    @GetMapping
    public ResponseEntity<ResponseObject> getCart(
            @RequestParam(value = "user_id", required = false) Long userId,
            @RequestParam(value = "session_id", required = false) String sessionId) {
        try {
            // Kiểm tra quyền truy cập
            if (!hasCartAccess(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ResponseObject.builder()
                                .status(HttpStatus.FORBIDDEN)
                                .message("Không có quyền truy cập giỏ hàng này")
                                .build());
            }

            CartResponse cartResponse = cartService.getCart(userId, sessionId);
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Lấy thông tin giỏ hàng thành công")
                    .data(cartResponse)
                    .build());

        } catch (Exception e) {
            logger.error("Lỗi khi lấy thông tin giỏ hàng: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message("Lỗi khi lấy thông tin giỏ hàng: " + e.getMessage())
                            .build());
        }
    }

    // API lấy tóm tắt giỏ hàng (số lượng, tổng tiền)
    @GetMapping("/summary")
    public ResponseEntity<ResponseObject> getCartSummary(
            @RequestParam(value = "user_id", required = false) Long userId,
            @RequestParam(value = "session_id", required = false) String sessionId) {
        try {
            // Kiểm tra quyền truy cập
            if (!hasCartAccess(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ResponseObject.builder()
                                .status(HttpStatus.FORBIDDEN)
                                .message("Không có quyền truy cập giỏ hàng này")
                                .build());
            }

            CartSummaryResponse summaryResponse = cartService.getCartSummary(userId, sessionId);
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Lấy tóm tắt giỏ hàng thành công")
                    .data(summaryResponse)
                    .build());

        } catch (Exception e) {
            logger.error("Lỗi khi lấy tóm tắt giỏ hàng: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message("Lỗi khi lấy tóm tắt giỏ hàng: " + e.getMessage())
                            .build());
        }
    }

    // API xóa toàn bộ giỏ hàng
    @DeleteMapping("/clear")
    public ResponseEntity<ResponseObject> clearCart(
            @RequestParam(value = "user_id", required = false) Long userId,
            @RequestParam(value = "session_id", required = false) String sessionId) {
        try {
            // Kiểm tra quyền truy cập
            if (!hasCartAccess(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ResponseObject.builder()
                                .status(HttpStatus.FORBIDDEN)
                                .message("Không có quyền truy cập giỏ hàng này")
                                .build());
            }

            cartService.clearCart(userId, sessionId);
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Xóa toàn bộ giỏ hàng thành công")
                    .build());

        } catch (Exception e) {
            logger.error("Lỗi khi xóa toàn bộ giỏ hàng: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message("Lỗi khi xóa toàn bộ giỏ hàng: " + e.getMessage())
                            .build());
        }
    }

    // API chuyển giỏ hàng từ session sang user khi đăng nhập
    // Chỉ user đã đăng nhập mới có thể thực hiện
    @PostMapping("/merge")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> mergeCart(
            @RequestParam("user_id") Long userId,
            @RequestParam("session_id") String sessionId) {
        try {
            // Kiểm tra quyền truy cập - chỉ merge cho chính user đó
            if (!hasCartAccess(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ResponseObject.builder()
                                .status(HttpStatus.FORBIDDEN)
                                .message("Không có quyền merge giỏ hàng này")
                                .build());
            }

            CartResponse cartResponse = cartService.mergeCart(userId, sessionId);
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Gộp giỏ hàng thành công")
                    .data(cartResponse)
                    .build());

        } catch (Exception e) {
            logger.error("Lỗi khi gộp giỏ hàng: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message("Lỗi khi gộp giỏ hàng: " + e.getMessage())
                            .build());
        }
    }

//     API xóa cart item theo ID
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ResponseObject> removeCartItem(
            @PathVariable Long cartItemId,
            @RequestParam(value = "user_id", required = false) Long userId,
            @RequestParam(value = "session_id", required = false) String sessionId) {
        try {
            // Kiểm tra quyền truy cập
            if (!hasCartAccess(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ResponseObject.builder()
                                .status(HttpStatus.FORBIDDEN)
                                .message("Không có quyền truy cập giỏ hàng này")
                                .build());
            }

            CartResponse cartResponse = cartService.removeCartItem(cartItemId, userId, sessionId);
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Xóa mặt hàng khỏi giỏ hàng thành công")
                    .data(cartResponse)
                    .build());

        } catch (Exception e) {
            logger.error("Lỗi khi xóa mặt hàng khỏi giỏ hàng: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message("Lỗi khi xóa mặt hàng khỏi giỏ hàng: " + e.getMessage())
                            .build());
        }
    }

//     API tăng số lượng sản phẩm trong giỏ hàng
    @PutMapping("/increase/{productVariantId}")
    public ResponseEntity<ResponseObject> increaseQuantity(
            @PathVariable Long productVariantId,
            @RequestParam(value = "user_id", required = false) Long userId,
            @RequestParam(value = "session_id", required = false) String sessionId) {
        try {
            // Kiểm tra quyền truy cập
            if (!hasCartAccess(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ResponseObject.builder()
                                .status(HttpStatus.FORBIDDEN)
                                .message("Không có quyền truy cập giỏ hàng này")
                                .build());
            }

            CartResponse cartResponse = cartService.increaseQuantity(productVariantId, userId, sessionId);
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Tăng số lượng sản phẩm thành công")
                    .data(cartResponse)
                    .build());

        } catch (Exception e) {
            logger.error("Lỗi khi tăng số lượng sản phẩm: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message("Lỗi khi tăng số lượng sản phẩm: " + e.getMessage())
                            .build());
        }
    }

//     API giảm số lượng sản phẩm trong giỏ hàng
    @PutMapping("/decrease/{productVariantId}")
    public ResponseEntity<ResponseObject> decreaseQuantity(
            @PathVariable Long productVariantId,
            @RequestParam(value = "user_id", required = false) Long userId,
            @RequestParam(value = "session_id", required = false) String sessionId) {
        try {
            // Kiểm tra quyền truy cập
            if (!hasCartAccess(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ResponseObject.builder()
                                .status(HttpStatus.FORBIDDEN)
                                .message("Không có quyền truy cập giỏ hàng này")
                                .build());
            }

            CartResponse cartResponse = cartService.decreaseQuantity(productVariantId, userId, sessionId);
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Giảm số lượng sản phẩm thành công")
                    .data(cartResponse)
                    .build());

        } catch (Exception e) {
            logger.error("Lỗi khi giảm số lượng sản phẩm: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message("Lỗi khi giảm số lượng sản phẩm: " + e.getMessage())
                            .build());
        }
    }

    // API kiểm tra tính khả dụng của các sản phẩm trong giỏ hàng
    @GetMapping("/validate")
    public ResponseEntity<ResponseObject> validateCartItems(
            @RequestParam(value = "user_id", required = false) Long userId,
            @RequestParam(value = "session_id", required = false) String sessionId) {
        try {
            // Kiểm tra quyền truy cập
            if (!hasCartAccess(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ResponseObject.builder()
                                .status(HttpStatus.FORBIDDEN)
                                .message("Không có quyền truy cập giỏ hàng này")
                                .build());
            }

            CartResponse cartResponse = cartService.validateCartItems(userId, sessionId);
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Kiểm tra giỏ hàng thành công")
                    .data(cartResponse)
                    .build());

        } catch (Exception e) {
            logger.error("Lỗi khi kiểm tra giỏ hàng: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message("Lỗi khi kiểm tra giỏ hàng: " + e.getMessage())
                            .build());
        }
    }
}
