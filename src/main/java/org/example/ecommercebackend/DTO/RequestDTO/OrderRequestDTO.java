package org.example.ecommercebackend.DTO.RequestDTO;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDTO {

    @NotNull(message = "User id is required")
    private Integer userId;

    @NotNull(message = "At least one cart item is required")
    @Size(min = 1, message = "At least one item must be added to order")
    private List<CartItemRequestDTO> items;
}
