package org.example.ecommercebackend.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ecommercebackend.DTO.RequestDTO.WishlistItemRequestDTO;
import org.example.ecommercebackend.DTO.ResponseDTO.WishlistItemResponseDTO;
import org.example.ecommercebackend.Entity.*;
import org.example.ecommercebackend.Exception.BadRequestException;
import org.example.ecommercebackend.Exception.ResourceNotFoundException;
import org.example.ecommercebackend.Repository.WishlistItemRepository;
import org.example.ecommercebackend.Repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final UserService userService;
    private final ProductService productService;

    public Wishlist getOrCreateWishlist(Integer userId) {
        log.debug("Fetching or creating wishlist for userId={}", userId);

        User user = userService.getUserEntityById(userId);
        return wishlistRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    log.info("No existing wishlist found for userId={}; creating new wishlist", userId);
                    return wishlistRepository.save(
                            Wishlist.builder().user(user).build());
                });
    }

    @Transactional(readOnly = true)
    public List<WishlistItemResponseDTO> getWishlistItems(Integer userId) {
        log.debug("Fetching wishlist items for userId={}", userId);
        Wishlist wishlist = getOrCreateWishlist(userId);
        List<WishlistItemResponseDTO> items = wishlist.getItems().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        log.debug("Found {} wishlist item(s) for userId={}", items.size(), userId);
        return items;
    }

    public WishlistItemResponseDTO addItem(Integer userId, WishlistItemRequestDTO dto) {
        log.info("Adding productId={} to wishlist for userId={}", dto.getProductId(), userId);

        Wishlist wishlist = getOrCreateWishlist(userId);
        Product product = productService.getProductEntityById(dto.getProductId());

        boolean alreadyExists = wishlistItemRepository
                .findByWishlist_IdAndProduct_Id(wishlist.getId(), dto.getProductId())
                .isPresent();

        if (alreadyExists) {
            log.warn("Duplicate wishlist add attempt: productId={} already in wishlist for userId={}",
                    dto.getProductId(), userId);
            throw new BadRequestException("Product already in wishlist");
        }

        WishlistItem item = WishlistItem.builder()
                .wishlist(wishlist)
                .product(product)
                .build();

        WishlistItem saved = wishlistItemRepository.save(item);
        log.info("Wishlist item id={} added (productId={}) for userId={}",
                saved.getId(), dto.getProductId(), userId);

        return mapToResponse(saved);
    }

    public void removeItem(Integer userId, Integer itemId) {
        log.info("Removing wishlist itemId={} for userId={}", itemId, userId);

        Wishlist wishlist = getOrCreateWishlist(userId);

        WishlistItem item = wishlistItemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Cannot remove: wishlist item not found with id={}", itemId);
                    return new ResourceNotFoundException("Wishlist item not found with id: " + itemId);
                });

        if (!item.getWishlist().getId().equals(wishlist.getId())) {
            log.warn("Unauthorized removal attempt: itemId={} does not belong to userId={}", itemId, userId);
            throw new ResourceNotFoundException("Wishlist item does not belong to this user");
        }

        wishlistItemRepository.delete(item);
        log.info("Wishlist item id={} removed successfully for userId={}", itemId, userId);
    }

    private WishlistItemResponseDTO mapToResponse(WishlistItem item) {
        return WishlistItemResponseDTO.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .price(item.getProduct().getPrice())
                .inStock(item.getProduct().getStock() > 0)
                .build();
    }
}