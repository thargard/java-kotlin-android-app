package example.com.server.service;

import example.com.server.model.CartItem;
import example.com.server.model.Product;
import example.com.server.model.User;
import example.com.server.repository.CartItemRepository;
import example.com.server.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    public CartItem addToCart(Long userId, Long productId, Integer quantity) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Product product = productService.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        // Check if product is available
        if (!product.getIsAvailable()) {
            throw new IllegalArgumentException("Product is not available");
        }

        // Check if already in cart
        Optional<CartItem> existingItem = cartItemRepository.findByUserIdAndProductId(userId, productId);
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + (quantity != null ? quantity : 1));
            return cartItemRepository.save(item);
        }

        CartItem cartItem = new CartItem(user, product);
        cartItem.setQuantity(quantity != null ? quantity : 1);
        return cartItemRepository.save(cartItem);
    }

    public List<CartItem> getCartByUserId(Long userId) {
        return cartItemRepository.findByUserIdWithProduct(userId);
    }

    public Optional<CartItem> findById(Long id) {
        return cartItemRepository.findById(id);
    }

    public Optional<CartItem> updateCartItemQuantity(Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartItemId));

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            return Optional.empty();
        }

        cartItem.setQuantity(quantity);
        return Optional.of(cartItemRepository.save(cartItem));
    }

    public void removeFromCart(Long userId, Long productId) {
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
    }

    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    public long getCartItemCount(Long userId) {
        return cartItemRepository.countByUserId(userId);
    }

    /**
     * Checkout: create an order for each cart item, mark products as unavailable, then clear the cart.
     * @return list of created order IDs and total price
     */
    @Transactional
    public Map<String, Object> checkout(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserIdWithProduct(userId);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }
        List<Long> orderIds = new ArrayList<>();
        double totalPrice = 0;
        for (CartItem item : items) {
            Product product = item.getProduct();
            if (product == null) continue;
            if (!product.getIsAvailable()) {
                throw new IllegalArgumentException("Product is not available: " + product.getName());
            }
            String description = "Purchase: " + product.getName() + " (ID: " + product.getId() + "), qty: " + item.getQuantity();
            var order = orderService.createOrder(userId, description);
            orderIds.add(order.getId());
            totalPrice += item.getTotalPrice();
            productService.updateProduct(product.getId(), null, null, null, null, false);
        }
        cartItemRepository.deleteByUserId(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("orderIds", orderIds);
        result.put("totalPrice", totalPrice);
        result.put("message", "Checkout successful");
        return result;
    }
}
