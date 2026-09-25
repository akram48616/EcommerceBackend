package org.example.ecommercebackend.DTO.ResponseDTO;

import lombok.*;

import java.io.Serializable;
import java.security.SecureRandomParameters;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDTO implements Serializable {

    private Integer id;

    private String name;

    private String description;

    private Double price;

    private Integer stock;

    private String imageUrl;

    private Integer categoryId;

    private String categoryName;
    private Double averageRating;
    private Integer reviewCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}