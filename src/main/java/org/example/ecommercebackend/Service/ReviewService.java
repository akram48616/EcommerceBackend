package org.example.ecommercebackend.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ecommercebackend.DTO.RequestDTO.ReviewRequestDTO;
import org.example.ecommercebackend.DTO.ResponseDTO.ReviewResponseDTO;
import org.example.ecommercebackend.Entity.*;
import org.example.ecommercebackend.Exception.BadRequestException;
import org.example.ecommercebackend.Exception.ResourceNotFoundException;
import org.example.ecommercebackend.Repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final ProductService productService;

    public ReviewResponseDTO addReview(Integer userId, ReviewRequestDTO dto) {
        log.info("Adding review by userId={} for productId={}", userId, dto.getProductId());

        User user = userService.getUserEntityById(userId);
        Product product = productService.getProductEntityById(dto.getProductId());

        boolean alreadyReviewed = reviewRepository
                .findByUser_IdAndProduct_Id(userId, dto.getProductId())
                .isPresent();

        if (alreadyReviewed) {
            log.warn("Duplicate review attempt by userId={} for productId={}", userId, dto.getProductId());
            throw new BadRequestException("You have already reviewed this product. Use update instead.");
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Review id={} added by userId={} for productId={}, rating={}",
                saved.getId(), userId, dto.getProductId(), dto.getRating());

        return mapToResponse(saved);
    }

    public ReviewResponseDTO updateReview(Integer userId, Integer reviewId, ReviewRequestDTO dto) {
        log.info("Updating reviewId={} by userId={}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.warn("Cannot update: review not found with id={}", reviewId);
                    return new ResourceNotFoundException("Review not found with id: " + reviewId);
                });

        if (!review.getUser().getId().equals(userId)) {
            log.warn("Unauthorized update attempt on reviewId={} by userId={} (owned by userId={})",
                    reviewId, userId, review.getUser().getId());
            throw new BadRequestException("You can only update your own review");
        }

        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        Review updated = reviewRepository.save(review);
        log.info("Review id={} updated successfully", reviewId);

        return mapToResponse(updated);
    }

    public void deleteReview(Integer userId, Integer reviewId) {
        log.info("Deleting reviewId={} by userId={}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.warn("Cannot delete: review not found with id={}", reviewId);
                    return new ResourceNotFoundException("Review not found with id: " + reviewId);
                });

        if (!review.getUser().getId().equals(userId)) {
            log.warn("Unauthorized delete attempt on reviewId={} by userId={} (owned by userId={})",
                    reviewId, userId, review.getUser().getId());
            throw new BadRequestException("You can only delete your own review");
        }

        reviewRepository.delete(review);
        log.info("Review id={} deleted successfully", reviewId);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsByProduct(Integer productId) {
        log.debug("Fetching reviews for productId={}", productId);
        List<ReviewResponseDTO> reviews = reviewRepository.findByProduct_Id(productId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        log.debug("Found {} review(s) for productId={}", reviews.size(), productId);
        return reviews;
    }

    private ReviewResponseDTO mapToResponse(Review review) {
        return ReviewResponseDTO.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFirstname() + " " + review.getUser().getLastname())
                .productId(review.getProduct().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}