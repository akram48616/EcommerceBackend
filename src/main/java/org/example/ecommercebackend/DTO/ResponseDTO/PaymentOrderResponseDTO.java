package org.example.ecommercebackend.DTO.ResponseDTO;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentOrderResponseDTO {
    private String razorpayOrderId;
    private String razorpayKeyId;
    private Double amount;
    private String currency;
}