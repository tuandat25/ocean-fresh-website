package com.tuandat.oceanfresh_backend.services.cart;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tuandat.oceanfresh_backend.dtos.cart.AddToCartDTO;
import com.tuandat.oceanfresh_backend.dtos.cart.RemoveFromCartDTO;
import com.tuandat.oceanfresh_backend.dtos.cart.UpdateCartItemDTO;
import com.tuandat.oceanfresh_backend.exceptions.ResourceNotFoundException;
import com.tuandat.oceanfresh_backend.models.Cart;
import com.tuandat.oceanfresh_backend.models.CartItem;
import com.tuandat.oceanfresh_backend.models.ProductVariant;
import com.tuandat.oceanfresh_backend.repositories.CartItemRepository;
import com.tuandat.oceanfresh_backend.repositories.CartRepository;
import com.tuandat.oceanfresh_backend.repositories.ProductVariantRepository;
import com.tuandat.oceanfresh_backend.repositories.UserRepository;
import com.tuandat.oceanfresh_backend.responses.cart.CartItemResponse;
import com.tuandat.oceanfresh_backend.responses.cart.CartResponse;
import com.tuandat.oceanfresh_backend.responses.cart.CartSummaryResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService implements ICartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CartResponse addToCart(AddToCartDTO addToCartDTO) throws Exception {
        logger.info("Adding product variant {} to cart for user {} or session {}", 
                   addToCartDTO.getProductVariantId(), addToCartDTO.getUserId(), addToCartDTO.getSessionId());

        // Validate input
        validateCartIdentity(addToCartDTO.getUserId(), addToCartDTO.getSessionId());
        
        // Get or create cart
        Cart cart = getOrCreateCart(addToCartDTO.getUserId(), addToCartDTO.getSessionId());
        
        // Validate product variant
        ProductVariant productVariant = productVariantRepository.findById(addToCartDTO.getProductVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Biến thể sản phẩm", "id", addToCartDTO.getProductVariantId()));

        // Check if product is active and available
        if (!productVariant.isActive() || !productVariant.getProduct().isActive()) {
            throw new Exception("Sản phẩm không còn khả dụng");
        }

        // Check stock availability
        if (productVariant.getQuantityInStock() < addToCartDTO.getQuantity()) {
            throw new Exception("Không đủ hàng trong kho. Còn lại: " + productVariant.getQuantityInStock());
        }

        // Check if item already exists in cart
        Optional<CartItem> existingCartItem = cartItemRepository.findByCartIdAndProductVariantId(
                cart.getId(), addToCartDTO.getProductVariantId());

        if (existingCartItem.isPresent()) {
            // Update existing item
            CartItem cartItem = existingCartItem.get();
            int newQuantity = cartItem.getQuantity() + addToCartDTO.getQuantity();
            
            // Check total quantity doesn't exceed stock
            if (newQuantity > productVariant.getQuantityInStock()) {
                throw new Exception("Tổng số lượng vượt quá tồn kho. Còn lại: " + productVariant.getQuantityInStock());
            }
            
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
            logger.info("Updated existing cart item quantity to {}", newQuantity);
        } else {
            // Create new cart item
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .productVariant(productVariant)
                    .quantity(addToCartDTO.getQuantity())
                    .priceAtAddition(productVariant.getPrice())
                    .build();
            cartItemRepository.save(cartItem);
            logger.info("Added new cart item with quantity {}", addToCartDTO.getQuantity());
        }

        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(UpdateCartItemDTO updateCartItemDTO) throws Exception {
        logger.info("Updating cart item {} to quantity {}", 
                   updateCartItemDTO.getCartItemId(), updateCartItemDTO.getQuantity());

        // Validate input
        validateCartIdentity(updateCartItemDTO.getUserId(), updateCartItemDTO.getSessionId());

        // Find cart item
        CartItem cartItem = cartItemRepository.findById(updateCartItemDTO.getCartItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Mặt hàng trong giỏ", "id", updateCartItemDTO.getCartItemId()));

        // Verify ownership
        Cart cart = cartItem.getCart();
        validateCartOwnership(cart, updateCartItemDTO.getUserId(), updateCartItemDTO.getSessionId());

        // Check stock availability
        ProductVariant productVariant = cartItem.getProductVariant();
        if (updateCartItemDTO.getQuantity() > productVariant.getQuantityInStock()) {
            throw new Exception("Không đủ hàng trong kho. Còn lại: " + productVariant.getQuantityInStock());
        }

        // Update quantity
        cartItem.setQuantity(updateCartItemDTO.getQuantity());
        cartItemRepository.save(cartItem);

        logger.info("Updated cart item {} to quantity {}", updateCartItemDTO.getCartItemId(), updateCartItemDTO.getQuantity());
        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeFromCart(RemoveFromCartDTO removeFromCartDTO) throws Exception {
        logger.info("Removing product variant {} from cart for user {} or session {}", 
                   removeFromCartDTO.getProductVariantId(), removeFromCartDTO.getUserId(), removeFromCartDTO.getSessionId());

        // Validate input
        validateCartIdentity(removeFromCartDTO.getUserId(), removeFromCartDTO.getSessionId());

        // Find cart
        Optional<Cart> cartOpt = findCart(removeFromCartDTO.getUserId(), removeFromCartDTO.getSessionId());
        if (cartOpt.isEmpty()) {
            throw new ResourceNotFoundException("Giỏ hàng không tồn tại");
        }

        Cart cart = cartOpt.get();

        // Find and remove cart item
        Optional<CartItem> cartItemOpt = cartItemRepository.findByCartIdAndProductVariantId(
                cart.getId(), removeFromCartDTO.getProductVariantId());

        if (cartItemOpt.isPresent()) {
            cartItemRepository.delete(cartItemOpt.get());
            logger.info("Removed product variant {} from cart", removeFromCartDTO.getProductVariantId());
        } else {
            throw new ResourceNotFoundException("Sản phẩm không có trong giỏ hàng");
        }

        return buildCartResponse(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId, String sessionId) throws Exception {
        logger.info("Getting cart for user {} or session {}", userId, sessionId);

        validateCartIdentity(userId, sessionId);
        
        Optional<Cart> cartOpt = findCart(userId, sessionId);
        if (cartOpt.isEmpty()) {
            // Return empty cart
            return buildEmptyCartResponse(userId, sessionId);
        }

        return buildCartResponse(cartOpt.get());
    }

    @Override
    @Transactional(readOnly = true)
    public CartSummaryResponse getCartSummary(Long userId, String sessionId) throws Exception {
        logger.info("Getting cart summary for user {} or session {}", userId, sessionId);

        validateCartIdentity(userId, sessionId);

        Optional<Cart> cartOpt = findCart(userId, sessionId);
        if (cartOpt.isEmpty()) {
            return buildEmptyCartSummary();
        }

        Cart cart = cartOpt.get();
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        return mapToCartSumaryResponse(cartItems);
    }

    @Override
    @Transactional
    public void clearCart(Long userId, String sessionId) throws Exception {
        logger.info("Clearing cart for user {} or session {}", userId, sessionId);

        validateCartIdentity(userId, sessionId);

        Optional<Cart> cartOpt = findCart(userId, sessionId);
        if (cartOpt.isPresent()) {
            cartItemRepository.deleteByCartId(cartOpt.get().getId());
            logger.info("Cleared cart for user {} or session {}", userId, sessionId);
        }
    }

    @Override
    @Transactional
    public CartResponse mergeCart(Long userId, String sessionId) throws Exception {
        logger.info("Merging cart from session {} to user {}", sessionId, userId);

        if (userId == null) {
            throw new Exception("User ID không được để trống khi merge cart");
        }

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Người dùng", "id", userId);
        }

        // Get or create user cart
        Cart userCart = getOrCreateCart(userId, null);

        // Get session cart if exists
        Optional<Cart> sessionCartOpt = cartRepository.findBySessionId(sessionId);
        if (sessionCartOpt.isEmpty()) {
            return buildCartResponse(userCart);
        }

        Cart sessionCart = sessionCartOpt.get();
        List<CartItem> sessionCartItems = cartItemRepository.findByCartId(sessionCart.getId());

        // Merge items
        for (CartItem sessionItem : sessionCartItems) {
            Optional<CartItem> existingUserItem = cartItemRepository.findByCartIdAndProductVariantId(
                    userCart.getId(), sessionItem.getProductVariant().getId());

            if (existingUserItem.isPresent()) {
                // Merge quantities
                CartItem userItem = existingUserItem.get();
                int totalQuantity = userItem.getQuantity() + sessionItem.getQuantity();
                
                // Check stock
                if (totalQuantity <= sessionItem.getProductVariant().getQuantityInStock()) {
                    userItem.setQuantity(totalQuantity);
                    cartItemRepository.save(userItem);
                } else {
                    // Keep user item quantity if total exceeds stock
                    logger.warn("Cannot merge item {} - would exceed stock", sessionItem.getProductVariant().getSku());
                }
            } else {
                // Move session item to user cart
                sessionItem.setCart(userCart);
                cartItemRepository.save(sessionItem);
            }
        }

        // Delete session cart
        cartRepository.delete(sessionCart);

        logger.info("Successfully merged cart from session {} to user {}", sessionId, userId);
        return buildCartResponse(userCart);
    }

    @Override
    @Transactional
    public CartResponse removeCartItem(Long cartItemId, Long userId, String sessionId) throws Exception {
        logger.info("Removing cart item {} for user {} or session {}", cartItemId, userId, sessionId);

        validateCartIdentity(userId, sessionId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Mặt hàng trong giỏ", "id", cartItemId));

        Cart cart = cartItem.getCart();
        validateCartOwnership(cart, userId, sessionId);

        cartItemRepository.delete(cartItem);
        logger.info("Removed cart item {}", cartItemId);

        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse increaseQuantity(Long productVariantId, Long userId, String sessionId) throws Exception {
        return adjustQuantity(productVariantId, userId, sessionId, 1);
    }

    @Override
    @Transactional
    public CartResponse decreaseQuantity(Long productVariantId, Long userId, String sessionId) throws Exception {
        return adjustQuantity(productVariantId, userId, sessionId, -1);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse validateCartItems(Long userId, String sessionId) throws Exception {
        logger.info("Validating cart items for user {} or session {}", userId, sessionId);

        validateCartIdentity(userId, sessionId);

        Optional<Cart> cartOpt = findCart(userId, sessionId);
        if (cartOpt.isEmpty()) {
            return buildEmptyCartResponse(userId, sessionId);
        }

        Cart cart = cartOpt.get();
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        // Check each item for availability and stock
        for (CartItem item : cartItems) {
            ProductVariant variant = item.getProductVariant();
            if (!variant.isActive() || !variant.getProduct().isActive()) {
                logger.warn("Cart item {} is no longer available", item.getId());
            }
            if (item.getQuantity() > variant.getQuantityInStock()) {
                logger.warn("Cart item {} quantity exceeds stock", item.getId());
            }
        }

        return buildCartResponse(cart);
    }
 

    private Cart getOrCreateCart(Long userId, String sessionId) {
        Optional<Cart> cartOpt = findCart(userId, sessionId);
        
        if (cartOpt.isPresent()) {
            return cartOpt.get();
        }

        // Create new cart
        Cart.CartBuilder cartBuilder = Cart.builder();
        
        if (userId != null) {
            cartBuilder.userId(userId);
        } else {
            cartBuilder.sessionId(sessionId != null ? sessionId : generateSessionId());
        }

        Cart cart = cartRepository.save(cartBuilder.build());
        logger.info("Created new cart with ID: {}", cart.getId());
        return cart;
    }

    private Optional<Cart> findCart(Long userId, String sessionId) {
        if (userId != null) {
            return cartRepository.findByUserId(userId);
        } else if (StringUtils.hasText(sessionId)) {
            return cartRepository.findBySessionId(sessionId);
        }
        return Optional.empty();
    }

    private void validateCartIdentity(Long userId, String sessionId) throws Exception {
        if (userId == null && !StringUtils.hasText(sessionId)) {
            throw new Exception("Phải cung cấp user ID hoặc session ID");
        }
    }

    private void validateCartOwnership(Cart cart, Long userId, String sessionId) throws Exception {
        boolean isOwner = false;
        
        if (userId != null && cart.getUserId() != null) {
            isOwner = cart.getUserId().equals(userId);
        } else if (StringUtils.hasText(sessionId) && StringUtils.hasText(cart.getSessionId())) {
            isOwner = cart.getSessionId().equals(sessionId);
        }

        if (!isOwner) {
            throw new Exception("Không có quyền truy cập giỏ hàng này");
        }
    }

    private CartResponse adjustQuantity(Long productVariantId, Long userId, String sessionId, int adjustment) throws Exception {
        logger.info("Adjusting quantity for product variant {} by {} for user {} or session {}", 
                   productVariantId, adjustment, userId, sessionId);

        validateCartIdentity(userId, sessionId);

        Optional<Cart> cartOpt = findCart(userId, sessionId);
        if (cartOpt.isEmpty()) {
            throw new ResourceNotFoundException("Giỏ hàng không tồn tại");
        }

        Cart cart = cartOpt.get();
        Optional<CartItem> cartItemOpt = cartItemRepository.findByCartIdAndProductVariantId(
                cart.getId(), productVariantId);

        if (cartItemOpt.isEmpty()) {
            throw new ResourceNotFoundException("Sản phẩm không có trong giỏ hàng");
        }

        CartItem cartItem = cartItemOpt.get();
        int newQuantity = cartItem.getQuantity() + adjustment;

        if (newQuantity <= 0) {
            // Remove item if quantity becomes 0 or negative
            cartItemRepository.delete(cartItem);
            logger.info("Removed cart item as quantity became {}", newQuantity);
        } else {
            // Check stock
            if (newQuantity > cartItem.getProductVariant().getQuantityInStock()) {
                throw new Exception("Không đủ hàng trong kho. Còn lại: " + cartItem.getProductVariant().getQuantityInStock());
            }
            
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
            logger.info("Adjusted cart item quantity to {}", newQuantity);
        }

        return buildCartResponse(cart);
    }

    private CartResponse buildCartResponse(Cart cart) {
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());

        CartSummaryResponse summary = mapToCartSumaryResponse(cartItems);

        return CartResponse.builder()
                .cartId(cart.getId())
                .userId(cart.getUserId())
                .sessionId(cart.getSessionId())
                .cartItems(itemResponses)
                .totalItems(summary.getTotalItems())
                .totalQuantity(summary.getTotalQuantity())
                .subtotalAmount(summary.getSubtotalAmount())
                .estimatedShipping(summary.getEstimatedShipping())
                .estimatedTotal(summary.getEstimatedTotal())
                .isEmpty(summary.getIsEmpty())
                .createdAt(cart.getCreatedAt().format(FORMATTER))
                .updatedAt(cart.getUpdatedAt().format(FORMATTER))
                .build();
    }

    private CartItemResponse mapToCartItemResponse(CartItem cartItem) {
        ProductVariant variant = cartItem.getProductVariant();
        // BigDecimal lineTotal = cartItem.getPriceAtAddition().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        BigDecimal lineTotal = cartItem.getProductVariant().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return CartItemResponse.builder()
                .cartItemId(cartItem.getId())
                .productVariantId(variant.getId())
                .productId(variant.getProduct().getId())
                .productName(variant.getProduct().getName())
                .productSlug(variant.getProduct().getSlug())
                .variantName(variant.getVariantName())
                .variantSku(variant.getSku())
                .variantImage(variant.getThumbnailUrl())
                .mainImageUrl(variant.getProduct().getMainImageUrl())
                .unitPrice(variant.getPrice())
                .priceAtAddition(cartItem.getPriceAtAddition())
                .quantity(cartItem.getQuantity())
                .lineTotal(lineTotal)
                .stockQuantity(variant.getQuantityInStock())
                .isAvailable(variant.isActive() && variant.getProduct().isActive() && variant.getQuantityInStock() > 0)
                .createdAt(cartItem.getCreatedAt().format(FORMATTER))
                .updatedAt(cartItem.getUpdatedAt().format(FORMATTER))
                .build();
    }

    private CartSummaryResponse mapToCartSumaryResponse(List<CartItem> cartItems) {
        if (cartItems.isEmpty()) {
            return buildEmptyCartSummary();
        }

        int totalItems = cartItems.size();
        int totalQuantity = cartItems.stream().mapToInt(CartItem::getQuantity).sum();
        BigDecimal subtotalAmount = cartItems.stream()
                .map(item -> item.getPriceAtAddition().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal estimatedShipping = BigDecimal.ZERO;
        BigDecimal estimatedTotal = subtotalAmount;

        return CartSummaryResponse.builder()
                .totalItems(totalItems)
                .totalQuantity(totalQuantity)
                .subtotalAmount(subtotalAmount)
                .estimatedShipping(estimatedShipping)
                .estimatedTotal(estimatedTotal)
                .isEmpty(false)
                .build();
    }

    private CartResponse buildEmptyCartResponse(Long userId, String sessionId) {
        return CartResponse.builder()
                .cartId(null)
                .userId(userId)
                .sessionId(sessionId)
                .cartItems(List.of())
                .totalItems(0)
                .totalQuantity(0)
                .subtotalAmount(BigDecimal.ZERO)
                .estimatedShipping(BigDecimal.ZERO)
                .estimatedTotal(BigDecimal.ZERO)
                .isEmpty(true)
                .createdAt(null)
                .updatedAt(null)
                .build();
    }

    private CartSummaryResponse buildEmptyCartSummary() {
        return CartSummaryResponse.builder()
                .totalItems(0)
                .totalQuantity(0)
                .subtotalAmount(BigDecimal.ZERO)
                .estimatedShipping(BigDecimal.ZERO)
                .estimatedTotal(BigDecimal.ZERO)
                .isEmpty(true)
                .build();
    }

    private String generateSessionId() {
        return "sess_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
