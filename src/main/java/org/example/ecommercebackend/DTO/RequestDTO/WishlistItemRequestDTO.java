package org.example.ecommercebackend.DTO.RequestDTO;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class WishlistItemRequestDTO {
    @NotNull(message = "Product id is required")
    private Integer productId;
}