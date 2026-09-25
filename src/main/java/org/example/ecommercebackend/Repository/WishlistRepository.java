package org.example.ecommercebackend.Repository;

import org.example.ecommercebackend.Entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {
    Optional<Wishlist> findByUser_Id(Integer userId);
}