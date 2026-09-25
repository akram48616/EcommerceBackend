package org.example.ecommercebackend.Exception;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private Integer status;

    private String message;

    private String error;

    private LocalDateTime timestamp;

    private String path;
}
