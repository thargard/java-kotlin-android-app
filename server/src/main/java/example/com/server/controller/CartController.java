package example.com.server.controller;

import example.com.server.model.CartItem;
import example.com.server.service.CartService;
import example.com.server.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final JwtService jwtService;

    @Autowired
    public CartController(CartService cartService, JwtService jwtService) {
        this.cartService = cartService;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ResponseEntity<?> getCart(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        List<CartItem> cartItems = cartService.getCartByUserId(userId);

        double totalPrice = cartItems.stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();

        Map<String, Object> response = new HashMap<>();
        response.put("items", cartItems.stream().map(this::cartItemToMap).toList());
        response.put("totalItems", cartItems.size());
        response.put("totalPrice", totalPrice);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<?> updateCartItem(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long productId,
            @RequestBody Map<String, Object> body) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        try {
            Integer quantity = Integer.valueOf(body.get("quantity").toString());

            var updatedItem = cartService.updateCartItemQuantity(productId, quantity);

            if (updatedItem.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "message", "Item removed from cart",
                        "productId", productId
                ));
            }

            return ResponseEntity.ok(cartItemToMap(updatedItem.get()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeFromCart(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long productId) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        cartService.removeFromCart(userId, productId);

        return ResponseEntity.ok(Map.of(
                "message", "Item removed from cart",
                "productId", productId
        ));
    }

    @DeleteMapping
    public ResponseEntity<?> clearCart(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        cartService.clearCart(userId);

        return ResponseEntity.ok(Map.of("message", "Cart cleared"));
    }

    @GetMapping("/count")
    public ResponseEntity<?> getCartCount(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        long count = cartService.getCartItemCount(userId);

        return ResponseEntity.ok(Map.of("count", count));
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }
        try {
            Map<String, Object> result = cartService.checkout(userId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    private Map<String, Object> cartItemToMap(CartItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", item.getId());
        map.put("productId", item.getProduct().getId());
        map.put("productName", item.getProduct().getName());
        map.put("productImageUrl", item.getProduct().getImageUrl());
        map.put("quantity", item.getQuantity());
        map.put("unitPrice", item.getProduct().getPrice());
        map.put("totalPrice", item.getTotalPrice());
        return map;
    }
}
