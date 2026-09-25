package org.example.ecommercebackend.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecommercebackend.DTO.RequestDTO.CartItemRequestDTO;
import org.example.ecommercebackend.DTO.ResponseDTO.CartItemResponseDTO;
import org.example.ecommercebackend.Service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Cart", description = "Endpoints for managing a user's shopping cart")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get cart items for a user")
    @GetMapping("/{userId}/items")
    public ResponseEntity<List<CartItemResponseDTO>> getItems(@PathVariable Integer userId) {
        return ResponseEntity.ok(cartService.getCartItems(userId));
    }

    @Operation(summary = "Get cart total for a user")
    @GetMapping("/{userId}/total")
    public ResponseEntity<Double> getTotal(@PathVariable Integer userId) {
        return ResponseEntity.ok(cartService.getCartTotal(userId));
    }

    @Operation(summary = "Add an item to cart", description = "If the product already exists in cart, quantity is increased")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item added",
                    content = @Content(schema = @Schema(implementation = CartItemResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    @PostMapping("/{userId}/items")
    public ResponseEntity<CartItemResponseDTO> addItem(
            @PathVariable Integer userId,
            @Valid @RequestBody CartItemRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItemToCart(userId, dto));
    }

    @Operation(summary = "Update cart item quantity")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item updated"),
            @ApiResponse(responseCode = "404", description = "Cart item not found", content = @Content)
    })
    @PutMapping("/{userId}/items/{itemId}")
    public ResponseEntity<CartItemResponseDTO> updateItem(
            @PathVariable Integer userId,
            @PathVariable Integer itemId,
            @Valid @RequestBody CartItemRequestDTO dto) {
        return ResponseEntity.ok(cartService.updateCartItem(userId, itemId, dto));
    }

    @Operation(summary = "Remove an item from cart")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item removed"),
            @ApiResponse(responseCode = "404", description = "Cart item not found", content = @Content)
    })
    @DeleteMapping("/{userId}/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Integer userId, @PathVariable Integer itemId) {
        cartService.removeItemFromCart(userId, itemId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Clear entire cart")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cart cleared"),
            @ApiResponse(responseCode = "400", description = "Cart is already empty", content = @Content)
    })
    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable Integer userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}