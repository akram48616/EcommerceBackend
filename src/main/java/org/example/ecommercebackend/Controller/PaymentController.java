package org.example.ecommercebackend.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecommercebackend.DTO.RequestDTO.CreatePaymentRequestDTO;
import org.example.ecommercebackend.DTO.RequestDTO.VerifyPaymentRequestDTO;
import org.example.ecommercebackend.DTO.ResponseDTO.PaymentOrderResponseDTO;
import org.example.ecommercebackend.Service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payments", description = "Endpoints for creating and verifying Razorpay payments")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Create a Razorpay payment order",
            description = "Creates a payment order on Razorpay for an existing order; returns details needed by the frontend checkout widget")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment order created",
                    content = @Content(schema = @Schema(implementation = PaymentOrderResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Payment order creation failed", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    @PostMapping("/create")
    public ResponseEntity<PaymentOrderResponseDTO> createPaymentOrder(@Valid @RequestBody CreatePaymentRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPaymentOrder(dto));
    }

    @Operation(summary = "Verify a completed payment",
            description = "Verifies the Razorpay signature and marks the order as CONFIRMED if valid")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment verified successfully"),
            @ApiResponse(responseCode = "400", description = "Signature verification failed", content = @Content),
            @ApiResponse(responseCode = "404", description = "Payment record not found", content = @Content)
    })
    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(@Valid @RequestBody VerifyPaymentRequestDTO dto) {
        return ResponseEntity.ok(paymentService.verifyPayment(dto));
    }
}