package org.example.ecommercebackend.DTO.RequestDTO;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemRequestDTO {

    @NotNull(message = "Product id is required")
    private Integer productId;

    @Positive(message = "Quantity must be at least 1")
    private Integer quantity;
}