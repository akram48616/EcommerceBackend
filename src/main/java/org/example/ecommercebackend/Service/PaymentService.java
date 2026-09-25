package org.example.ecommercebackend.Service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ecommercebackend.DTO.RequestDTO.CreatePaymentRequestDTO;
import org.example.ecommercebackend.DTO.RequestDTO.VerifyPaymentRequestDTO;
import org.example.ecommercebackend.DTO.ResponseDTO.PaymentOrderResponseDTO;
import org.example.ecommercebackend.Entity.Payment;
import org.example.ecommercebackend.Exception.BadRequestException;
import org.example.ecommercebackend.Exception.ResourceNotFoundException;
import org.example.ecommercebackend.Repository.PaymentRepository;
import org.example.ecommercebackend.Repository.OrderRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final RazorpayClient razorpayClient;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;


    // ============================================================
    // CREATE PAYMENT ORDER
    // ============================================================

    @Transactional
    public PaymentOrderResponseDTO createPaymentOrder(
            CreatePaymentRequestDTO dto) {

        log.info(
                "Creating payment order for orderId={}",
                dto.getOrderId()
        );

        org.example.ecommercebackend.Entity.Order order =
                orderRepository.findById(dto.getOrderId())
                        .orElseThrow(() -> {

                            log.warn(
                                    "Payment creation failed. Order not found: orderId={}",
                                    dto.getOrderId()
                            );

                            return new ResourceNotFoundException(
                                    "Order not found with id: "
                                            + dto.getOrderId()
                            );
                        });

        log.debug(
                "Order found for payment: orderId={}, amount={}",
                order.getId(),
                order.getTotalAmount()
        );

        try {

            JSONObject orderRequest =
                    new JSONObject();

            // Razorpay expects amount in the smallest currency unit
            // paise, not rupees
            long amountInPaise =
                    Math.round(order.getTotalAmount() * 100);

            orderRequest.put(
                    "amount",
                    amountInPaise
            );

            orderRequest.put(
                    "currency",
                    "INR"
            );

            orderRequest.put(
                    "receipt",
                    "order_rcpt_" + order.getId()
            );

            log.debug(
                    "Sending payment order request to Razorpay: orderId={}, amountInPaise={}",
                    order.getId(),
                    amountInPaise
            );

            Order razorpayOrder =
                    razorpayClient.orders.create(
                            orderRequest
                    );

            String razorpayOrderId =
                    razorpayOrder.get("id");

            log.info(
                    "Razorpay order created successfully: orderId={}, razorpayOrderId={}",
                    order.getId(),
                    razorpayOrderId
            );

            Payment payment =
                    Payment.builder()
                            .order(order)
                            .razorpayOrderId(
                                    razorpayOrderId
                            )
                            .status(
                                    Payment.PaymentStatus.CREATED
                            )
                            .amount(
                                    order.getTotalAmount()
                            )
                            .createdAt(
                                    LocalDateTime.now()
                            )
                            .build();

            paymentRepository.save(payment);

            log.info(
                    "Payment record saved successfully: orderId={}, razorpayOrderId={}, status={}",
                    order.getId(),
                    razorpayOrderId,
                    Payment.PaymentStatus.CREATED
            );

            return PaymentOrderResponseDTO.builder()
                    .razorpayOrderId(
                            razorpayOrderId
                    )
                    .razorpayKeyId(
                            keyId
                    )
                    .amount(
                            order.getTotalAmount()
                    )
                    .currency("INR")
                    .build();

        } catch (Exception e) {

            log.error(
                    "Failed to create Razorpay payment order: orderId={}",
                    dto.getOrderId(),
                    e
            );

            throw new BadRequestException(
                    "Payment order creation failed: "
                            + e.getMessage()
            );
        }
    }


    // ============================================================
    // VERIFY PAYMENT
    // ============================================================

    @Transactional
    public String verifyPayment(
            VerifyPaymentRequestDTO dto) {

        log.info(
                "Starting payment verification for razorpayOrderId={}",
                dto.getRazorpayOrderId()
        );

        Payment payment =
                paymentRepository
                        .findByRazorpayOrderId(
                                dto.getRazorpayOrderId()
                        )
                        .orElseThrow(() -> {

                            log.warn(
                                    "Payment verification failed. Payment record not found: razorpayOrderId={}",
                                    dto.getRazorpayOrderId()
                            );

                            return new ResourceNotFoundException(
                                    "Payment record not found"
                            );
                        });

        try {

            JSONObject options =
                    new JSONObject();

            options.put(
                    "razorpay_order_id",
                    dto.getRazorpayOrderId()
            );

            options.put(
                    "razorpay_payment_id",
                    dto.getRazorpayPaymentId()
            );

            options.put(
                    "razorpay_signature",
                    dto.getRazorpaySignature()
            );

            log.debug(
                    "Verifying Razorpay payment signature: razorpayOrderId={}",
                    dto.getRazorpayOrderId()
            );

            boolean isValid =
                    Utils.verifyPaymentSignature(
                            options,
                            keySecret
                    );

            if (isValid) {

                payment.setRazorpayPaymentId(
                        dto.getRazorpayPaymentId()
                );

                payment.setRazorpaySignature(
                        dto.getRazorpaySignature()
                );

                payment.setStatus(
                        Payment.PaymentStatus.SUCCESS
                );

                paymentRepository.save(payment);

                org.example.ecommercebackend.Entity.Order order =
                        payment.getOrder();

                order.setStatus(
                        org.example.ecommercebackend.Entity.Order.OrderStatus.CONFIRMED
                );

                orderRepository.save(order);

                log.info(
                        "Payment verified successfully: orderId={}, razorpayOrderId={}, status={}",
                        order.getId(),
                        dto.getRazorpayOrderId(),
                        Payment.PaymentStatus.SUCCESS
                );

                return "Payment verified successfully";

            } else {

                payment.setStatus(
                        Payment.PaymentStatus.FAILED
                );

                paymentRepository.save(payment);

                log.warn(
                        "Payment signature verification failed: razorpayOrderId={}, status={}",
                        dto.getRazorpayOrderId(),
                        Payment.PaymentStatus.FAILED
                );

                throw new BadRequestException(
                        "Payment signature verification failed"
                );
            }

        } catch (BadRequestException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Payment verification error: razorpayOrderId={}",
                    dto.getRazorpayOrderId(),
                    e
            );

            throw new BadRequestException(
                    "Payment verification failed: "
                            + e.getMessage()
            );
        }
    }
}