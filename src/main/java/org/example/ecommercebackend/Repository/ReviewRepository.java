package org.example.ecommercebackend.Repository;

import org.example.ecommercebackend.Entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByProduct_Id(Integer productId);
    Optional<Review> findByUser_IdAndProduct_Id(Integer userId, Integer productId);
}