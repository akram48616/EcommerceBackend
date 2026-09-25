package org.example.ecommercebackend.Repository;

import org.example.ecommercebackend.Entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
}