package example.com.server.controller;

import example.com.server.model.CartItem;
import example.com.server.model.Product;
import example.com.server.model.User;
import example.com.server.service.CartService;
import example.com.server.service.JwtService;
import example.com.server.service.MessageService;
import example.com.server.service.OrderService;
import example.com.server.service.ProductService;
import example.com.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final JwtService jwtService;
    private final UserService userService;
    private final CartService cartService;
    private final OrderService orderService;
    private final MessageService messageService;

    @Autowired
    public ProductController(ProductService productService, JwtService jwtService, UserService userService,
                             CartService cartService, OrderService orderService, MessageService messageService) {
        this.productService = productService;
        this.jwtService = jwtService;
        this.userService = userService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.messageService = messageService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(defaultValue = "false") boolean availableOnly) {

        Page<Product> products = productService.findAllWithFilters(category, search, sellerId, availableOnly, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("content", products.getContent());
        response.put("totalElements", products.getTotalElements());
        response.put("totalPages", products.getTotalPages());
        response.put("page", products.getNumber());
        response.put("size", products.getSize());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.findById(id);
        if (product.isPresent()) {
            return ResponseEntity.ok(productToMap(product.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Product not found"));
        }
    }

    @PostMapping
    public ResponseEntity<?> createProduct(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> body) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        try {
            String name = (String) body.get("name");
            String description = (String) body.get("description");
            String category = (String) body.get("category");
            String imageUrl = (String) body.get("imageUrl");

            Double price = null;
            if (body.get("price") != null) {
                price = Double.valueOf(body.get("price").toString());
            }

            if (name == null || price == null || category == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Name, price, and category are required"));
            }

            Product product = productService.createProduct(userId, name, description, price, category, imageUrl);
            return ResponseEntity.status(HttpStatus.CREATED).body(productToMap(product));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        Optional<Product> existingProduct = productService.findById(id);
        if (existingProduct.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Product not found"));
        }

        Product product = existingProduct.get();
        // Check if the user is the owner
        if (!product.getSeller().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You can only update your own products"));
        }

        try {
            String name = (String) body.get("name");
            String description = (String) body.get("description");
            String category = (String) body.get("category");
            Boolean isAvailable = body.get("isAvailable") != null
                    ? Boolean.valueOf(body.get("isAvailable").toString())
                    : null;

            Double price = null;
            if (body.get("price") != null) {
                price = Double.valueOf(body.get("price").toString());
            }

            Product updatedProduct = productService.updateProduct(id, name, description, price, category, isAvailable);
            return ResponseEntity.ok(productToMap(updatedProduct));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        Optional<Product> existingProduct = productService.findById(id);
        if (existingProduct.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Product not found"));
        }

        Product product = existingProduct.get();
        // Check if the user is the owner
        if (!product.getSeller().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You can only delete your own products"));
        }

        try {
            productService.deleteProduct(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/cart")
    public ResponseEntity<?> addToCart(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        Optional<Product> product = productService.findById(id);
        if (product.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Product not found"));
        }

        try {
            Integer quantity = 1;
            if (body.get("quantity") != null) {
                quantity = Integer.valueOf(body.get("quantity").toString());
            }

            CartItem cartItem = cartService.addToCart(userId, id, quantity);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product added to cart");
            response.put("cartItemId", cartItem.getId());
            response.put("productId", id);
            response.put("quantity", cartItem.getQuantity());
            response.put("totalPrice", cartItem.getTotalPrice());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/buy")
    public ResponseEntity<?> buyProduct(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        Optional<Product> productOpt = productService.findById(id);
        if (productOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Product not found"));
        }

        Product product = productOpt.get();
        if (!product.getIsAvailable()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Product is not available"));
        }

        // Create an order for this product
        String description = "Purchase: " + product.getName() + " (ID: " + product.getId() + ")";
        var order = orderService.createOrder(userId, description);

        // Mark product as sold (not available)
        productService.updateProduct(id, null, null, null, null, false);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Purchase successful");
        response.put("orderId", order.getId());
        response.put("productId", id);
        response.put("productName", product.getName());
        response.put("price", product.getPrice());
        response.put("sellerId", product.getSeller().getId());
        response.put("sellerName", product.getSeller().getFullName() != null ? product.getSeller().getFullName() : product.getSeller().getLogin());
        response.put("orderStatus", order.getStatus().name());
        response.put("createdAt", order.getCreatedAt());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/contact")
    public ResponseEntity<?> contactSeller(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        Optional<Product> productOpt = productService.findById(id);
        if (productOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Product not found"));
        }

        Product product = productOpt.get();
        Long sellerId = product.getSeller().getId();

        // Don't allow contacting yourself
        if (sellerId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "You cannot contact yourself"));
        }

        String message = "Hi, I'm interested in your product: " + product.getName();
        if (body.get("message") != null) {
            message = body.get("message").toString();
        }

        // Start a conversation
        var firstMessage = messageService.startConversation(userId, sellerId, id, message);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Chat started with seller");
        response.put("threadId", firstMessage.getThreadId());
        response.put("productId", id);
        response.put("sellerId", sellerId);
        response.put("sellerName", product.getSeller().getFullName() != null ? product.getSeller().getFullName() : product.getSeller().getLogin());
        response.put("messageId", firstMessage.getId());
        response.put("createdAt", firstMessage.getCreatedAt());

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> productToMap(Product product) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", product.getId());
        map.put("name", product.getName());
        map.put("description", product.getDescription());
        map.put("price", product.getPrice());
        map.put("imageUrl", product.getImageUrl());
        map.put("sellerId", product.getSeller().getId());
        map.put("sellerName", product.getSeller().getFullName() != null ? product.getSeller().getFullName() : product.getSeller().getLogin());
        map.put("category", product.getCategory());
        map.put("createdAt", product.getCreatedAt());
        map.put("isAvailable", product.getIsAvailable());
        return map;
    }
}
