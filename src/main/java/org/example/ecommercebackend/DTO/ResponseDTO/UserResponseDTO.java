package org.example.ecommercebackend.DTO.ResponseDTO;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO implements Serializable {
    private Integer id;
    private String firstname;
    private String lastname;
    private String email;
    private String role;
}