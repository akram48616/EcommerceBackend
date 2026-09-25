package org.example.ecommercebackend.DTO.ResponseDTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponseDTO implements java.io.Serializable {

    private Integer id;

    private Integer productId;

    private String productName;

    private Integer quantity;

    private Double unitPrice;

    private Double totalPrice;
}