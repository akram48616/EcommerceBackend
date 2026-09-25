package org.example.ecommercebackend.DTO.RequestDTO;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CreatePaymentRequestDTO {
    @NotNull(message = "Order id is required")
    private Integer orderId;
}