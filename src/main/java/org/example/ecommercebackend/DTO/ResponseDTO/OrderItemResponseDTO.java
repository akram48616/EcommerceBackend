package org.example.ecommercebackend.DTO.ResponseDTO;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponseDTO implements Serializable {

    private Integer id;

    private Integer orderId;

    private Integer productId;

    private String productName;

    private Double unitPrice;

    private Integer quantity;

    private Double totalPrice;
}