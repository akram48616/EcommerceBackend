package org.example.ecommercebackend.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecommercebackend.DTO.RequestDTO.WishlistItemRequestDTO;
import org.example.ecommercebackend.DTO.ResponseDTO.WishlistItemResponseDTO;
import org.example.ecommercebackend.Service.WishlistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Wishlist", description = "Endpoints for managing a user's saved-for-later products")
@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @Operation(summary = "Get wishlist items for a user")
    @GetMapping("/{userId}/items")
    public ResponseEntity<List<WishlistItemResponseDTO>> getItems(@PathVariable Integer userId) {
        return ResponseEntity.ok(wishlistService.getWishlistItems(userId));
    }

    @Operation(summary = "Add a product to wishlist")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item added",
                    content = @Content(schema = @Schema(implementation = WishlistItemResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Product already in wishlist", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    @PostMapping("/{userId}/items")
    public ResponseEntity<WishlistItemResponseDTO> addItem(
            @PathVariable Integer userId,
            @Valid @RequestBody WishlistItemRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wishlistService.addItem(userId, dto));
    }

    @Operation(summary = "Remove a product from wishlist")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item removed"),
            @ApiResponse(responseCode = "404", description = "Wishlist item not found", content = @Content)
    })
    @DeleteMapping("/{userId}/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Integer userId, @PathVariable Integer itemId) {
        wishlistService.removeItem(userId, itemId);
        return ResponseEntity.noContent().build();
    }
}