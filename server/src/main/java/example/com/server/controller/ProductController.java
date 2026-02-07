package example.com.server.controller;

import example.com.server.model.Product;
import example.com.server.model.User;
import example.com.server.service.JwtService;
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

    @Autowired
    public ProductController(ProductService productService, JwtService jwtService, UserService userService) {
        this.productService = productService;
        this.jwtService = jwtService;
        this.userService = userService;
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
            @PathVariable Long id) {
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

        // For now, just return success - in a real implementation, this would add to user's cart
        return ResponseEntity.ok(Map.of(
                "message", "Product added to cart",
                "productId", id,
                "userId", userId
        ));
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

        Optional<Product> product = productService.findById(id);
        if (product.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Product not found"));
        }

        Product p = product.get();
        if (!p.getIsAvailable()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Product is not available"));
        }

        // For now, just return order details - in a real implementation, this would create an order
        return ResponseEntity.ok(Map.of(
                "message", "Purchase successful",
                "orderId", System.currentTimeMillis(), // Mock order ID
                "productId", id,
                "productName", p.getName(),
                "price", p.getPrice(),
                "sellerId", p.getSeller().getId(),
                "sellerName", p.getSeller().getFullName() != null ? p.getSeller().getFullName() : p.getSeller().getLogin()
        ));
    }

    @PostMapping("/{id}/contact")
    public ResponseEntity<?> contactSeller(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id) {
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

        Product p = product.get();
        // For now, just return chat details - in a real implementation, this would create/start a chat
        return ResponseEntity.ok(Map.of(
                "message", "Chat started with seller",
                "threadId", System.currentTimeMillis(), // Mock thread ID
                "productId", id,
                "sellerId", p.getSeller().getId(),
                "sellerName", p.getSeller().getFullName() != null ? p.getSeller().getFullName() : p.getSeller().getLogin()
        ));
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
