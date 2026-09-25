package org.example.ecommercebackend.DTO.RequestDTO;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LogoutRequestDTO {
    private String refreshToken;
}