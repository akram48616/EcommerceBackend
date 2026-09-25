package org.example.ecommercebackend.DTO.ResponseDTO;

import lombok.*;

import java.io.Serializable;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class WishlistItemResponseDTO implements Serializable {
    private Integer id;
    private Integer productId;
    private String productName;
    private Double price;
    private Boolean inStock;
}