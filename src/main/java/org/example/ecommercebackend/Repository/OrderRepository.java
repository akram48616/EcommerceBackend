package org.example.ecommercebackend.Repository;

import org.example.ecommercebackend.Entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    Page<Order> findByUser_Id(Integer userId, Pageable pageable);
    List<Order> findByUser_Id(Integer userId);
    List<Order> findByStatus(Order.OrderStatus status);
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status <> 'CANCELLED'")
    Double getTotalRevenue();

    @Query("SELECT o.status, COUNT(o), SUM(o.totalAmount) FROM Order o GROUP BY o.status")
    List<Object[]> getRevenueByStatus();

    @Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity) as totalSold " +
            "FROM OrderItem oi GROUP BY oi.product.id, oi.product.name ORDER BY totalSold DESC")
    List<Object[]> getTopSellingProducts();
}