package org.example.ecommercebackend.DTO.ResponseDTO;

import lombok.*;
import java.io.Serializable;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ChartDataDTO implements Serializable {
    private List<String> labels;
    private List<Double> values;
}