package org.example.ecommercebackend.Repository;

import org.example.ecommercebackend.Entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    Page<Product> findByCategoryId(Integer categoryId, Pageable pageable);
    Page<Product> findByNameContaining(String keyword, Pageable pageable);
    List<Product> findByCategory_Id(Integer categoryId);
    List<Product> findByStockLessThan(Integer threshold);
}