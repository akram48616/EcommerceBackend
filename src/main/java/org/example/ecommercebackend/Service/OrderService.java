package org.example.ecommercebackend.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ecommercebackend.DTO.RequestDTO.OrderRequestDTO;
import org.example.ecommercebackend.DTO.ResponseDTO.OrderItemResponseDTO;
import org.example.ecommercebackend.DTO.ResponseDTO.OrderResponseDTO;
import org.example.ecommercebackend.Entity.Cart;
import org.example.ecommercebackend.Entity.CartItem;
import org.example.ecommercebackend.Entity.Order;
import org.example.ecommercebackend.Entity.OrderItem;
import org.example.ecommercebackend.Entity.User;
import org.example.ecommercebackend.Exception.BadRequestException;
import org.example.ecommercebackend.Exception.EmptyCartException;
import org.example.ecommercebackend.Exception.ResourceNotFoundException;
import org.example.ecommercebackend.Repository.OrderRepository;
import org.example.ecommercebackend.Repository.OrderItemRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;
    private final UserService userService;
    private final ProductService productService;
    private final ModelMapper modelMapper;
    private final EmailService emailService;

    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO) {
        log.info("Creating order for userId={}", orderRequestDTO.getUserId());

        User user = userService.getUserEntityById(orderRequestDTO.getUserId());

        Cart cart = cartService.getCartEntityByUserId(orderRequestDTO.getUserId());
        if (cart.getItems().isEmpty()) {
            log.warn("Order creation failed for userId={}: cart is empty", orderRequestDTO.getUserId());
            throw new EmptyCartException("Cannot create order. Cart is empty");
        }

        Double totalAmount = cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        log.debug("Calculated totalAmount={} for userId={} with {} cart item(s)",
                totalAmount, orderRequestDTO.getUserId(), cart.getItems().size());

        Order order = Order.builder()
                .user(user)
                .totalAmount(totalAmount)
                .status(Order.OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Order created with id={} for userId={}, status={}",
                savedOrder.getId(), orderRequestDTO.getUserId(), savedOrder.getStatus());

        List<OrderItem> savedItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getProduct().getPrice())
                    .build();
            savedItems.add(orderItemRepository.save(orderItem));

            log.debug("Reducing stock for productId={} by quantity={} (orderId={})",
                    cartItem.getProduct().getId(), cartItem.getQuantity(), savedOrder.getId());
            productService.reduceStock(cartItem.getProduct().getId(), cartItem.getQuantity());
        }

        savedOrder.setItems(savedItems);

        cartService.clearCart(orderRequestDTO.getUserId());
        log.debug("Cart cleared for userId={}", orderRequestDTO.getUserId());

        try {
            emailService.sendOrderConfirmationEmail(user.getEmail(), user.getFirstname(),
                    savedOrder.getId(), savedOrder.getTotalAmount(), savedOrder.getStatus().name());
            log.info("Order confirmation email sent for orderId={} to {}", savedOrder.getId(), user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email for orderId={}", savedOrder.getId(), e);
        }

        return mapOrderToResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Integer orderId) {
        log.debug("Fetching order by id={}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found with id={}", orderId);
                    return new ResourceNotFoundException("Order not found with id: " + orderId);
                });
        return mapOrderToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByUserId(Integer userId) {
        log.debug("Fetching all orders for userId={}", userId);
        userService.getUserEntityById(userId);
        List<OrderResponseDTO> orders = orderRepository.findByUser_Id(userId).stream()
                .map(this::mapOrderToResponse)
                .collect(Collectors.toList());
        log.debug("Found {} order(s) for userId={}", orders.size(), userId);
        return orders;
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getOrdersByUserIdWithPagination(Integer userId, Pageable pageable) {
        log.debug("Fetching paginated orders for userId={}, page={}, size={}",
                userId, pageable.getPageNumber(), pageable.getPageSize());
        userService.getUserEntityById(userId);
        return orderRepository.findByUser_Id(userId, pageable)
                .map(this::mapOrderToResponse);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders() {
        log.debug("Fetching all orders");
        List<OrderResponseDTO> orders = orderRepository.findAll().stream()
                .map(this::mapOrderToResponse)
                .collect(Collectors.toList());
        log.debug("Retrieved {} order(s) total", orders.size());
        return orders;
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getAllOrdersWithPagination(Pageable pageable) {
        log.debug("Fetching paginated orders, page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        return orderRepository.findAll(pageable)
                .map(this::mapOrderToResponse);
    }

    public OrderResponseDTO updateOrderStatus(Integer orderId, Order.OrderStatus status) {
        log.info("Updating status for orderId={} to {}", orderId, status);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Cannot update status: order not found with id={}", orderId);
                    return new ResourceNotFoundException("Order not found with id: " + orderId);
                });

        Order.OrderStatus previousStatus = order.getStatus();
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order id={} status changed from {} to {}", orderId, previousStatus, status);

        return mapOrderToResponse(updatedOrder);
    }

    public void cancelOrder(Integer orderId) {
        log.info("Attempting to cancel orderId={}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Cannot cancel: order not found with id={}", orderId);
                    return new ResourceNotFoundException("Order not found with id: " + orderId);
                });

        if (order.getStatus() == Order.OrderStatus.DELIVERED || order.getStatus() == Order.OrderStatus.CANCELLED) {
            log.warn("Cannot cancel orderId={}: current status is {}", orderId, order.getStatus());
            throw new BadRequestException("Cannot cancel order with status: " + order.getStatus());
        }

        order.setStatus(Order.OrderStatus.CANCELLED);

        for (OrderItem orderItem : order.getItems()) {
            log.debug("Restocking productId={} by quantity={} due to cancellation of orderId={}",
                    orderItem.getProduct().getId(), orderItem.getQuantity(), orderId);
            productService.increaseStock(orderItem.getProduct().getId(), orderItem.getQuantity());
        }

        orderRepository.save(order);
        log.info("Order id={} cancelled successfully", orderId);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByStatus(Order.OrderStatus status) {
        log.debug("Fetching orders with status={}", status);
        List<OrderResponseDTO> orders = orderRepository.findByStatus(status).stream()
                .map(this::mapOrderToResponse)
                .collect(Collectors.toList());
        log.debug("Found {} order(s) with status={}", orders.size(), status);
        return orders;
    }

    private OrderResponseDTO mapOrderToResponse(Order order) {
        OrderResponseDTO dto = OrderResponseDTO.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .userName(order.getUser().getFirstname() + " " + order.getUser().getLastname())
                .totalPrice(order.getTotalAmount())
                .status(order.getStatus().toString())
                .orderDate(order.getCreatedAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getCreatedAt())
                .orderItems(order.getItems().stream()
                        .map(item -> OrderItemResponseDTO.builder()
                                .id(item.getId())
                                .orderId(item.getOrder().getId())
                                .productId(item.getProduct().getId())
                                .productName(item.getProduct().getName())
                                .unitPrice(item.getUnitPrice())
                                .quantity(item.getQuantity())
                                .totalPrice(item.getUnitPrice() * item.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();
        return dto;
    }
}