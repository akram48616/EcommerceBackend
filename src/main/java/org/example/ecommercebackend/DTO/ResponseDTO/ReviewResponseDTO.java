package org.example.ecommercebackend.DTO.ResponseDTO;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewResponseDTO implements Serializable {
    private Integer id;
    private Integer userId;
    private String userName;
    private Integer productId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}