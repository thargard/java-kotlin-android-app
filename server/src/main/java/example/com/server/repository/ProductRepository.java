package example.com.server.repository;

import example.com.server.model.Product;
import example.com.server.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    List<Product> findBySeller(User seller);

    List<Product> findBySellerId(Long sellerId);

    List<Product> findByCategory(String category);

    Page<Product> findByCategory(String category, Pageable pageable);

    Page<Product> findBySellerId(Long sellerId, Pageable pageable);

    Page<Product> findByIsAvailableTrue(Pageable pageable);

    Page<Product> findByCategoryAndIsAvailableTrue(String category, Pageable pageable);
}
