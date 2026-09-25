package org.example.ecommercebackend.Repository;

import org.example.ecommercebackend.Entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Integer> {
    Optional<WishlistItem> findByWishlist_IdAndProduct_Id(Integer wishlistId, Integer productId);
}