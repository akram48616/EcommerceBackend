package org.example.ecommercebackend.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecommercebackend.DTO.RequestDTO.ReviewRequestDTO;
import org.example.ecommercebackend.DTO.ResponseDTO.ReviewResponseDTO;
import org.example.ecommercebackend.Service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Reviews", description = "Endpoints for product reviews and ratings")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Add a review", description = "One review per user per product")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Review added",
                    content = @Content(schema = @Schema(implementation = ReviewResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Already reviewed this product", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    @PostMapping("/{userId}")
    public ResponseEntity<ReviewResponseDTO> addReview(
            @PathVariable Integer userId,
            @Valid @RequestBody ReviewRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.addReview(userId, dto));
    }

    @Operation(summary = "Update your own review")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review updated"),
            @ApiResponse(responseCode = "400", description = "Not your review", content = @Content),
            @ApiResponse(responseCode = "404", description = "Review not found", content = @Content)
    })
    @PutMapping("/{userId}/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable Integer userId,
            @PathVariable Integer reviewId,
            @Valid @RequestBody ReviewRequestDTO dto) {
        return ResponseEntity.ok(reviewService.updateReview(userId, reviewId, dto));
    }

    @Operation(summary = "Delete your own review")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Review deleted"),
            @ApiResponse(responseCode = "400", description = "Not your review", content = @Content),
            @ApiResponse(responseCode = "404", description = "Review not found", content = @Content)
    })
    @DeleteMapping("/{userId}/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Integer userId, @PathVariable Integer reviewId) {
        reviewService.deleteReview(userId, reviewId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all reviews for a product")
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponseDTO>> getByProduct(@PathVariable Integer productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
    }
}