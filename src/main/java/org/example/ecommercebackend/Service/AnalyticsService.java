package org.example.ecommercebackend.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ecommercebackend.DTO.ResponseDTO.*;
import org.example.ecommercebackend.Entity.Order;
import org.example.ecommercebackend.Entity.Product;
import org.example.ecommercebackend.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnalyticsService {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public DashboardSummaryDTO getDashboardSummary() {

        log.info("Fetching dashboard summary");

        Double revenue = orderRepository.getTotalRevenue();
        long lowStockCount =
                productRepository.findByStockLessThan(LOW_STOCK_THRESHOLD).size();

        DashboardSummaryDTO summary = DashboardSummaryDTO.builder()
                .totalRevenue(revenue != null ? revenue : 0.0)
                .totalOrders(orderRepository.count())
                .totalUsers(userRepository.count())
                .totalProducts(productRepository.count())
                .lowStockCount(lowStockCount)
                .build();

        log.info(
                "Dashboard summary fetched successfully: revenue={}, orders={}, users={}, products={}, lowStockCount={}",
                summary.getTotalRevenue(),
                summary.getTotalOrders(),
                summary.getTotalUsers(),
                summary.getTotalProducts(),
                summary.getLowStockCount()
        );

        return summary;
    }

    public ChartDataDTO getRevenueByStatusChart() {

        log.info("Fetching revenue by order status");

        List<Object[]> results = orderRepository.getRevenueByStatus();

        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        for (Object[] row : results) {

            Order.OrderStatus status = (Order.OrderStatus) row[0];
            Double totalAmount = (Double) row[2];

            labels.add(status.name());
            values.add(totalAmount != null ? totalAmount : 0.0);
        }

        log.info(
                "Revenue by status chart generated successfully with {} statuses",
                labels.size()
        );

        return ChartDataDTO.builder()
                .labels(labels)
                .values(values)
                .build();
    }

    public List<TopProductDTO> getTopSellingProducts(int limit) {

        log.info("Fetching top selling products with limit={}", limit);

        List<Object[]> results = orderRepository.getTopSellingProducts();

        List<TopProductDTO> topProducts = results.stream()
                .limit(limit)
                .map(row -> TopProductDTO.builder()
                        .productId((Integer) row[0])
                        .productName((String) row[1])
                        .totalSold((Long) row[2])
                        .build())
                .collect(Collectors.toList());

        log.info(
                "Top selling products fetched successfully: count={}",
                topProducts.size()
        );

        return topProducts;
    }

    public List<LowStockProductDTO> getLowStockProducts() {

        log.info(
                "Fetching low stock products with threshold={}",
                LOW_STOCK_THRESHOLD
        );

        List<Product> products =
                productRepository.findByStockLessThan(LOW_STOCK_THRESHOLD);

        List<LowStockProductDTO> lowStockProducts = products.stream()
                .map(p -> LowStockProductDTO.builder()
                        .productId(p.getId())
                        .productName(p.getName())
                        .currentStock(p.getStock())
                        .build())
                .collect(Collectors.toList());

        log.info(
                "Low stock products fetched successfully: count={}",
                lowStockProducts.size()
        );

        return lowStockProducts;
    }
}