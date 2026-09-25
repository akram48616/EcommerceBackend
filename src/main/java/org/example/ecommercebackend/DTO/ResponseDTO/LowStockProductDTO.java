package org.example.ecommercebackend.DTO.ResponseDTO;

import lombok.*;
import java.io.Serializable;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LowStockProductDTO implements Serializable {
    private Integer productId;
    private String productName;
    private Integer currentStock;
}