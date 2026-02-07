package example.com.server.service;

import example.com.server.model.Product;
import example.com.server.model.User;
import example.com.server.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserService userService;

    public Product createProduct(Long sellerId, String name, String description, Double price,
                                  String category, String imageUrl) {
        User seller = userService.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found: " + sellerId));

        Product product = new Product(name, description, price, imageUrl, seller, category);
        return productRepository.save(product);
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Page<Product> findAllPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return productRepository.findAll(pageable);
    }

    public Page<Product> findAllWithFilters(String category, String search, Long sellerId,
                                            Boolean availableOnly, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // For simplicity, we'll fetch all products and filter in memory for search functionality
        // In production, you'd want to use JPA Specifications or a more sophisticated approach
        List<Product> products;

        if (category != null && !category.isEmpty()) {
            products = productRepository.findByCategory(category);
        } else if (sellerId != null) {
            products = productRepository.findBySellerId(sellerId);
        } else if (availableOnly != null && availableOnly) {
            // Fetch all available products
            products = productRepository.findAll().stream()
                    .filter(Product::getIsAvailable)
                    .toList();
        } else {
            products = productRepository.findAll();
        }

        // Apply search filter if provided
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            products = products.stream()
                    .filter(product ->
                            (product.getName() != null && product.getName().toLowerCase().contains(searchLower)) ||
                                    (product.getDescription() != null && product.getDescription().toLowerCase().contains(searchLower)))
                    .toList();
        }

        // Simple pagination for in-memory filtering
        int start = page * size;
        int end = Math.min(start + size, products.size());
        List<Product> pagedProducts = start < products.size()
                ? products.subList(start, end)
                : List.of();

        return new org.springframework.data.domain.PageImpl<>(pagedProducts, pageable, products.size());
    }

    public Product updateProduct(Long id, String name, String description, Double price,
                                 String category, Boolean isAvailable) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        if (name != null) {
            product.setName(name);
        }
        if (description != null) {
            product.setDescription(description);
        }
        if (price != null) {
            product.setPrice(price);
        }
        if (category != null) {
            product.setCategory(category);
        }
        if (isAvailable != null) {
            product.setIsAvailable(isAvailable);
        }

        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }

    public List<Product> findBySellerId(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }
}
