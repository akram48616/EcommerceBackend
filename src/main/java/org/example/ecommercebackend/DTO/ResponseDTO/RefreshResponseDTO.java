package org.example.ecommercebackend.DTO.ResponseDTO;

import lombok.*;

import java.io.Serializable;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshResponseDTO implements Serializable {
    private String accessToken;
    private String refreshToken;
    private String type;
    private long accessTokenExpiresIn;
}