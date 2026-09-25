package org.example.ecommercebackend.DTO.ResponseDTO;

import lombok.*;
import java.io.Serializable;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TopProductDTO implements Serializable {
    private Integer productId;
    private String productName;
    private Long totalSold;
}