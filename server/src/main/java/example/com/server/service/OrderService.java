package example.com.server.service;

import example.com.server.model.Order;
import example.com.server.model.User;
import example.com.server.repository.OrderRepository;
import example.com.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private static final String DEFAULT_SORT = "createdAt,desc";
    private static final List<String> ALLOWED_SORT_FIELDS = List.of("id", "createdAt", "status");

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    public Order createOrder(Long userId, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        Order order = new Order(user, description, Order.Status.PENDING);
        return orderRepository.save(order);
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    /**
     * Find orders with optional filters, search, and sort.
     * @param userId filter by user id (optional)
     * @param status filter by status (optional)
     * @param search substring match on description, case-insensitive (optional)
     * @param createdAfter orders created after this instant (optional)
     * @param createdBefore orders created before this instant (optional)
     * @param sortParam e.g. "createdAt,desc" or "status,asc"; default "createdAt,desc"
     */
    public List<Order> findOrders(Long userId, Order.Status status, String search,
                                  Instant createdAfter, Instant createdBefore, String sortParam) {
        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (search != null && !search.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + search.toLowerCase() + "%"));
            }
            if (createdAfter != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdAfter));
            }
            if (createdBefore != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdBefore));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        Sort sort = parseSort(sortParam);
        return orderRepository.findAll(spec, sort);
    }

    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            sortParam = DEFAULT_SORT;
        }
        String[] parts = sortParam.split(",");
        String field = parts[0].trim();
        if (!ALLOWED_SORT_FIELDS.contains(field)) {
            field = "createdAt";
        }
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(direction, field);
    }

    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> findByStatus(Order.Status status) {
        return orderRepository.findByStatus(status);
    }

    public Order updateStatus(Long orderId, Order.Status status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public Order updateOrder(Long orderId, String description, Order.Status status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        if (description != null) {
            order.setDescription(description);
        }
        if (status != null) {
            order.setStatus(status);
        }
        return orderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new IllegalArgumentException("Order not found: " + id);
        }
        orderRepository.deleteById(id);
    }
}
