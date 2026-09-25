package org.example.ecommercebackend.DTO.ResponseDTO;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponseDTO implements Serializable {
    private Integer id;
    private Integer userId;
    private List<CartItemResponseDTO> items;
    private Double total;
}