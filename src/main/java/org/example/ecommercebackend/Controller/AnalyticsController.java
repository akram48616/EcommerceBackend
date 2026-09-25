package org.example.ecommercebackend.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.ecommercebackend.DTO.ResponseDTO.*;
import org.example.ecommercebackend.Service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Analytics", description = "Dashboard and reporting endpoints — ADMIN only")
@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Get dashboard summary")
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getSummary() {
        return ResponseEntity.ok(analyticsService.getDashboardSummary());
    }

    @Operation(summary = "Get revenue breakdown by order status (chart-ready)")
    @GetMapping("/revenue-by-status")
    public ResponseEntity<ChartDataDTO> getRevenueByStatus() {
        return ResponseEntity.ok(analyticsService.getRevenueByStatusChart());
    }

    @Operation(summary = "Get top-selling products")
    @GetMapping("/top-products")
    public ResponseEntity<List<TopProductDTO>> getTopProducts(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(analyticsService.getTopSellingProducts(limit));
    }

    @Operation(summary = "Get low-stock products (below 5 units)")
    @GetMapping("/low-stock")
    public ResponseEntity<List<LowStockProductDTO>> getLowStock() {
        return ResponseEntity.ok(analyticsService.getLowStockProducts());
    }
}