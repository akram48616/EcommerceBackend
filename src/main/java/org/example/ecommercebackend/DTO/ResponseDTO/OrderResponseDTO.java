package org.example.ecommercebackend.DTO.ResponseDTO;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDTO implements Serializable {

    private Integer id;

    private Integer userId;

    private String userName;

    private Double totalPrice;

    private String status;

    private LocalDateTime orderDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<OrderItemResponseDTO> orderItems;
}