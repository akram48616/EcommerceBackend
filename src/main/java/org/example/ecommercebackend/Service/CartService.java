package org.example.ecommercebackend.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ecommercebackend.DTO.RequestDTO.CartItemRequestDTO;
import org.example.ecommercebackend.DTO.ResponseDTO.CartItemResponseDTO;
import org.example.ecommercebackend.DTO.ResponseDTO.CartResponseDTO;
import org.example.ecommercebackend.Entity.Cart;
import org.example.ecommercebackend.Entity.CartItem;
import org.example.ecommercebackend.Entity.Product;
import org.example.ecommercebackend.Entity.User;
import org.example.ecommercebackend.Exception.EmptyCartException;
import org.example.ecommercebackend.Exception.ResourceNotFoundException;
import org.example.ecommercebackend.Repository.CartItemRepository;
import org.example.ecommercebackend.Repository.CartRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final UserService userService;
    private final ModelMapper modelMapper;

    public Cart getOrCreateCart(Integer userId) {

        log.debug("Fetching cart for userId={}", userId);

        User user = userService.getUserEntityById(userId);

        return cartRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    log.info("No cart found for userId={}, creating new cart", userId);

                    Cart cart = Cart.builder()
                            .user(user)
                            .build();

                    Cart savedCart = cartRepository.save(cart);

                    log.info(
                            "New cart created successfully: cartId={}, userId={}",
                            savedCart.getId(),
                            userId
                    );

                    return savedCart;
                });
    }

    public CartItemResponseDTO addItemToCart(
            Integer userId,
            CartItemRequestDTO cartItemRequestDTO) {

        log.info(
                "Adding product to cart: userId={}, productId={}, quantity={}",
                userId,
                cartItemRequestDTO.getProductId(),
                cartItemRequestDTO.getQuantity()
        );

        Cart cart = getOrCreateCart(userId);

        Product product =
                productService.getProductEntityById(
                        cartItemRequestDTO.getProductId()
                );

        CartItem existingItem = cart.getItems().stream()
                .filter(item ->
                        item.getProduct()
                                .getId()
                                .equals(cartItemRequestDTO.getProductId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {

            existingItem.setQuantity(
                    existingItem.getQuantity()
                            + cartItemRequestDTO.getQuantity()
            );

            CartItem updatedItem =
                    cartItemRepository.save(existingItem);

            log.info(
                    "Existing cart item quantity updated: cartItemId={}, newQuantity={}",
                    updatedItem.getId(),
                    updatedItem.getQuantity()
            );

            return mapCartItemToResponse(updatedItem);
        }

        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(cartItemRequestDTO.getQuantity())
                .build();

        CartItem savedItem =
                cartItemRepository.save(cartItem);

        log.info(
                "New item added to cart successfully: cartItemId={}, cartId={}, productId={}, quantity={}",
                savedItem.getId(),
                cart.getId(),
                product.getId(),
                savedItem.getQuantity()
        );

        return mapCartItemToResponse(savedItem);
    }

    public CartItemResponseDTO updateCartItem(
            Integer userId,
            Integer cartItemId,
            CartItemRequestDTO cartItemRequestDTO) {

        log.info(
                "Updating cart item: userId={}, cartItemId={}, newQuantity={}",
                userId,
                cartItemId,
                cartItemRequestDTO.getQuantity()
        );

        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> {
                    log.warn(
                            "Cart item not found: cartItemId={}",
                            cartItemId
                    );

                    return new ResourceNotFoundException(
                            "Cart item not found with id: " + cartItemId
                    );
                });

        if (!cartItem.getCart().getId().equals(cart.getId())) {

            log.warn(
                    "Cart item does not belong to user's cart: userId={}, cartItemId={}, cartId={}",
                    userId,
                    cartItemId,
                    cart.getId()
            );

            throw new ResourceNotFoundException(
                    "Cart item does not belong to this cart"
            );
        }

        cartItem.setQuantity(
                cartItemRequestDTO.getQuantity()
        );

        CartItem updatedItem =
                cartItemRepository.save(cartItem);

        log.info(
                "Cart item updated successfully: cartItemId={}, quantity={}",
                updatedItem.getId(),
                updatedItem.getQuantity()
        );

        return mapCartItemToResponse(updatedItem);
    }

    public void removeItemFromCart(
            Integer userId,
            Integer cartItemId) {

        log.info(
                "Removing cart item: userId={}, cartItemId={}",
                userId,
                cartItemId
        );

        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> {
                    log.warn(
                            "Cart item not found for removal: cartItemId={}",
                            cartItemId
                    );

                    return new ResourceNotFoundException(
                            "Cart item not found with id: " + cartItemId
                    );
                });

        if (!cartItem.getCart().getId().equals(cart.getId())) {

            log.warn(
                    "Unauthorized cart item removal attempt: userId={}, cartItemId={}, cartId={}",
                    userId,
                    cartItemId,
                    cart.getId()
            );

            throw new ResourceNotFoundException(
                    "Cart item does not belong to this cart"
            );
        }

        cartItemRepository.delete(cartItem);

        log.info(
                "Cart item removed successfully: cartItemId={}, userId={}",
                cartItemId,
                userId
        );
    }

    @Transactional(readOnly = true)
    public List<CartItemResponseDTO> getCartItems(Integer userId) {

        log.debug("Fetching cart items for userId={}", userId);

        Cart cart = getOrCreateCart(userId);

        List<CartItemResponseDTO> items = cart.getItems().stream()
                .map(this::mapCartItemToResponse)
                .collect(Collectors.toList());

        log.info(
                "Cart items fetched successfully: userId={}, itemCount={}",
                userId,
                items.size()
        );

        return items;
    }

    @Transactional(readOnly = true)
    public Double getCartTotal(Integer userId) {

        log.debug("Calculating cart total for userId={}", userId);

        Cart cart = getOrCreateCart(userId);

        Double total = cart.getItems().stream()
                .mapToDouble(item ->
                        item.getProduct().getPrice()
                                * item.getQuantity())
                .sum();

        log.info(
                "Cart total calculated: userId={}, total={}",
                userId,
                total
        );

        return total;
    }

    public void clearCart(Integer userId) {

        log.info("Clearing cart for userId={}", userId);

        Cart cart = getOrCreateCart(userId);

        if (cart.getItems().isEmpty()) {

            log.warn(
                    "Attempt to clear an already empty cart: userId={}",
                    userId
            );

            throw new EmptyCartException(
                    "Cart is already empty"
            );
        }

        int itemCount = cart.getItems().size();

        cartItemRepository.deleteAll(cart.getItems());

        log.info(
                "Cart cleared successfully: userId={}, removedItems={}",
                userId,
                itemCount
        );
    }

    public void validateCart(Integer userId) {

        log.debug(
                "Validating cart for checkout: userId={}",
                userId
        );

        Cart cart = getOrCreateCart(userId);

        if (cart.getItems().isEmpty()) {

            log.warn(
                    "Checkout validation failed: cart is empty for userId={}",
                    userId
            );

            throw new EmptyCartException(
                    "Cannot proceed with checkout. Cart is empty"
            );
        }

        log.info(
                "Cart validation successful for checkout: userId={}, itemCount={}",
                userId,
                cart.getItems().size()
        );
    }

    @Transactional(readOnly = true)
    public Cart getCartEntityByUserId(Integer userId) {

        log.debug(
                "Fetching cart entity for userId={}",
                userId
        );

        return getOrCreateCart(userId);
    }

    public CartResponseDTO viewCart(Integer userId) {

        log.info("Viewing cart for userId={}", userId);

        Cart cart = getOrCreateCart(userId);

        List<CartItemResponseDTO> items = cart.getItems().stream()
                .map(this::mapCartItemToResponse)
                .collect(Collectors.toList());

        Double total = cart.getItems().stream()
                .mapToDouble(item ->
                        item.getProduct().getPrice()
                                * item.getQuantity())
                .sum();

        log.info(
                "Cart viewed successfully: userId={}, cartId={}, itemCount={}, total={}",
                userId,
                cart.getId(),
                items.size(),
                total
        );

        return CartResponseDTO.builder()
                .id(cart.getId())
                .userId(userId)
                .items(items)
                .total(total)
                .build();
    }

    private CartItemResponseDTO mapCartItemToResponse(
            CartItem cartItem) {

        log.debug(
                "Mapping cart item to response: cartItemId={}, productId={}",
                cartItem.getId(),
                cartItem.getProduct().getId()
        );

        return CartItemResponseDTO.builder()
                .id(cartItem.getId())
                .productId(cartItem.getProduct().getId())
                .productName(cartItem.getProduct().getName())
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getProduct().getPrice())
                .totalPrice(
                        cartItem.getProduct().getPrice()
                                * cartItem.getQuantity()
                )
                .build();
    }
}