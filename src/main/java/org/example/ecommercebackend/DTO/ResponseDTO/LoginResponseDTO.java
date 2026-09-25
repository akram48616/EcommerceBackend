package org.example.ecommercebackend.DTO.ResponseDTO;

import lombok.*;

import java.io.Serializable;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginResponseDTO implements Serializable {
    private String token;
    private String refreshToken;
    private String type;
    private String email;
    private String role;
    private long accessTokenExpiresIn;
    private long refreshTokenExpiresIn;
}