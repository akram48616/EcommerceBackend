package org.example.ecommercebackend.DTO.ResponseDTO;

import lombok.*;
import java.io.Serializable;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardSummaryDTO implements Serializable {
    private Double totalRevenue;
    private Long totalOrders;
    private Long totalUsers;
    private Long totalProducts;
    private Long lowStockCount;
}