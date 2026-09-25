package org.example.ecommercebackend.DTO.ResponseDTO;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponseDTO implements Serializable {
    private Integer id;
    private String name;
    private String description;
}