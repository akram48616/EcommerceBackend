package org.example.ecommercebackend.DTO.RequestDTO;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
}