// ============================================================
// ProductService.java
// ============================================================

package org.example.ecommercebackend.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ecommercebackend.DTO.RequestDTO.ProductRequestDTO;
import org.example.ecommercebackend.DTO.ResponseDTO.ProductResponseDTO;
import org.example.ecommercebackend.Entity.Category;
import org.example.ecommercebackend.Entity.Product;
import org.example.ecommercebackend.Entity.Review;
import org.example.ecommercebackend.Exception.InsufficientStockException;
import org.example.ecommercebackend.Exception.ResourceNotFoundException;
import org.example.ecommercebackend.Repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ModelMapper modelMapper;


    // ============================================================
    // CREATE PRODUCT
    // ============================================================

    @CacheEvict(
            value = {
                    "products",
                    "allProducts",
                    "productsPagination",
                    "productsByCategory"
            },
            allEntries = true
    )
    public ProductResponseDTO createProduct(
            ProductRequestDTO productRequestDTO) {

        log.info("Creating product '{}' in categoryId={}",
                productRequestDTO.getName(), productRequestDTO.getCategoryId());

        Category category =
                categoryService.getCategoryEntityById(
                        productRequestDTO.getCategoryId()
                );

        Product product =
                Product.builder()
                        .name(productRequestDTO.getName())
                        .description(productRequestDTO.getDescription())
                        .price(productRequestDTO.getPrice())
                        .stock(productRequestDTO.getStock())
                        .imageUrl(productRequestDTO.getImageUrl())
                        .category(category)
                        .createdAt(LocalDateTime.now())
                        .build();

        Product savedProduct =
                productRepository.save(product);

        log.info("Product created with id={}, name='{}'", savedProduct.getId(), savedProduct.getName());

        return mapProductToResponse(savedProduct);
    }


    // ============================================================
    // GET PRODUCT BY ID
    // ============================================================

    @Cacheable(value = "products", key = "#productId")
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(
            Integer productId) {

        log.debug("Fetching product by id={}", productId);

        Product product =
                productRepository.findById(productId)
                        .orElseThrow(() -> {
                            log.warn("Product not found with id={}", productId);
                            return new ResourceNotFoundException(
                                    "Product not found with id: "
                                            + productId
                            );
                        });

        return mapProductToResponse(product);
    }


    // ============================================================
    // GET ALL PRODUCTS
    // ============================================================

    @Cacheable(value = "allProducts")
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getAllProducts() {

        log.debug("Fetching all products");

        List<ProductResponseDTO> products = productRepository.findAll()
                .stream()
                .map(this::mapProductToResponse)
                .collect(Collectors.toList());

        log.debug("Retrieved {} product(s)", products.size());

        return products;
    }


    // ============================================================
    // GET ALL PRODUCTS WITH PAGINATION
    // ============================================================

    @Cacheable(value = "productsPagination", key = "#pageable")
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getAllProductsWithPagination(
            Pageable pageable) {

        log.debug("Fetching paginated products, page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        return productRepository.findAll(pageable)
                .map(this::mapProductToResponse);
    }


    // ============================================================
    // GET PRODUCTS BY CATEGORY
    // ============================================================

    @Cacheable(value = "productsByCategory", key = "#categoryId")
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getProductsByCategory(
            Integer categoryId) {

        log.debug("Fetching products for categoryId={}", categoryId);

        // Make sure category exists
        categoryService.getCategoryEntityById(categoryId);

        List<ProductResponseDTO> products = productRepository
                .findByCategory_Id(categoryId)
                .stream()
                .map(this::mapProductToResponse)
                .collect(Collectors.toList());

        log.debug("Found {} product(s) in categoryId={}", products.size(), categoryId);

        return products;
    }


    // ============================================================
    // UPDATE PRODUCT
    // ============================================================

    @CacheEvict(
            value = {
                    "products",
                    "allProducts",
                    "productsPagination",
                    "productsByCategory"
            },
            allEntries = true
    )
    public ProductResponseDTO updateProduct(
            Integer productId,
            ProductRequestDTO productRequestDTO) {

        log.info("Updating productId={}", productId);

        Product product =
                productRepository.findById(productId)
                        .orElseThrow(() -> {
                            log.warn("Cannot update: product not found with id={}", productId);
                            return new ResourceNotFoundException(
                                    "Product not found with id: "
                                            + productId
                            );
                        });

        Category category =
                categoryService.getCategoryEntityById(
                        productRequestDTO.getCategoryId()
                );

        product.setName(
                productRequestDTO.getName()
        );

        product.setDescription(
                productRequestDTO.getDescription()
        );

        product.setPrice(
                productRequestDTO.getPrice()
        );

        product.setStock(
                productRequestDTO.getStock()
        );

        product.setImageUrl(
                productRequestDTO.getImageUrl()
        );

        product.setCategory(category);

        Product updatedProduct =
                productRepository.save(product);

        log.info("Product id={} updated successfully", productId);

        return mapProductToResponse(updatedProduct);
    }


    // ============================================================
    // DELETE PRODUCT
    // ============================================================

    @CacheEvict(
            value = {
                    "products",
                    "allProducts",
                    "productsPagination",
                    "productsByCategory"
            },
            allEntries = true
    )
    public void deleteProduct(Integer productId) {

        log.info("Deleting productId={}", productId);

        Product product =
                productRepository.findById(productId)
                        .orElseThrow(() -> {
                            log.warn("Cannot delete: product not found with id={}", productId);
                            return new ResourceNotFoundException(
                                    "Product not found with id: "
                                            + productId
                            );
                        });

        productRepository.delete(product);

        log.info("Product id={} deleted successfully", productId);
    }


    // ============================================================
    // REDUCE STOCK
    // ============================================================

    @CacheEvict(
            value = {
                    "products",
                    "allProducts",
                    "productsPagination",
                    "productsByCategory"
            },
            allEntries = true
    )
    public void reduceStock(
            Integer productId,
            Integer quantity) {

        log.debug("Reducing stock for productId={} by quantity={}", productId, quantity);

        Product product =
                productRepository.findById(productId)
                        .orElseThrow(() -> {
                            log.warn("Cannot reduce stock: product not found with id={}", productId);
                            return new ResourceNotFoundException(
                                    "Product not found with id: "
                                            + productId
                            );
                        });

        if (product.getStock() < quantity) {

            log.warn("Insufficient stock for productId={} ('{}'). Available={}, Requested={}",
                    productId, product.getName(), product.getStock(), quantity);

            throw new InsufficientStockException(
                    "Insufficient stock for product: "
                            + product.getName()
                            + ". Available: "
                            + product.getStock()
                            + ", Requested: "
                            + quantity
            );
        }

        product.setStock(
                product.getStock() - quantity
        );

        productRepository.save(product);

        log.debug("Stock reduced for productId={}. New stock={}", productId, product.getStock());
    }


    // ============================================================
    // INCREASE STOCK
    // ============================================================

    @CacheEvict(
            value = {
                    "products",
                    "allProducts",
                    "productsPagination",
                    "productsByCategory"
            },
            allEntries = true
    )
    public void increaseStock(
            Integer productId,
            Integer quantity) {

        log.debug("Increasing stock for productId={} by quantity={}", productId, quantity);

        Product product =
                productRepository.findById(productId)
                        .orElseThrow(() -> {
                            log.warn("Cannot increase stock: product not found with id={}", productId);
                            return new ResourceNotFoundException(
                                    "Product not found with id: "
                                            + productId
                            );
                        });

        product.setStock(
                product.getStock() + quantity
        );

        productRepository.save(product);

        log.debug("Stock increased for productId={}. New stock={}", productId, product.getStock());
    }


    // ============================================================
    // GET PRODUCT ENTITY BY ID
    // ============================================================

    @Transactional(readOnly = true)
    public Product getProductEntityById(
            Integer productId) {

        return productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product entity not found with id={}", productId);
                    return new ResourceNotFoundException(
                            "Product not found with id: "
                                    + productId
                    );
                });
    }


    // ============================================================
    // MAP PRODUCT TO RESPONSE DTO
    // ============================================================

    private ProductResponseDTO mapProductToResponse(
            Product product) {

        ProductResponseDTO dto =
                modelMapper.map(
                        product,
                        ProductResponseDTO.class
                );

        // Category name
        if (product.getCategory() != null) {

            dto.setCategoryName(
                    product.getCategory().getName()
            );
        }

        // Reviews
        List<Review> reviews =
                product.getReviews();

        if (reviews != null && !reviews.isEmpty()) {

            double avg =
                    reviews.stream()
                            .mapToInt(Review::getRating)
                            .average()
                            .orElse(0.0);

            dto.setAverageRating(
                    Math.round(avg * 10.0) / 10.0
            );

            dto.setReviewCount(
                    reviews.size()
            );

        } else {

            dto.setAverageRating(0.0);
            dto.setReviewCount(0);
        }

        return dto;
    }
}