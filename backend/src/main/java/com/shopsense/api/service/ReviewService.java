package com.shopsense.api.service;

import com.shopsense.api.dto.ReviewDtos.*;
import com.shopsense.api.entity.*;
import com.shopsense.api.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {
    private final ReviewRepository reviews;
    private final ProductRepository products;
    private final UserRepository users;

    public ReviewService(ReviewRepository reviews, ProductRepository products, UserRepository users) {
        this.reviews = reviews;
        this.products = products;
        this.users = users;
    }

    @Transactional
    public ReviewResponse create(String email, CreateReviewRequest request) {
        Product product = products.findById(request.productId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        User user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        reviews.findByProductAndUser(product, user).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already reviewed this product");
        });

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(request.rating());
        review.setReviewText(request.reviewText().trim());
        review = reviews.save(review);

        List<Review> all = reviews.findByProductOrderByCreatedAtDesc(product);
        BigDecimal avg = all.stream().map(r -> BigDecimal.valueOf(r.getRating())).reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(Math.max(all.size(), 1)), 2, java.math.RoundingMode.HALF_UP);
        product.setRating(avg);
        product.setReviewCount(all.size());
        products.save(product);

        return new ReviewResponse(review.getId(), product.getId(), user.getFirstName() + " " + user.getLastName(), review.getRating(), review.getReviewText(), review.getCreatedAt());
    }

    public ReviewSummaryResponse getForProduct(UUID productId) {
        Product product = products.findById(productId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        List<Review> all = reviews.findByProductOrderByCreatedAtDesc(product);
        List<ReviewResponse> responses = all.stream().map(r -> new ReviewResponse(
            r.getId(), r.getProduct().getId(), r.getUser().getFirstName() + " " + r.getUser().getLastName(), r.getRating(), r.getReviewText(), r.getCreatedAt()
        )).toList();
        double average = all.isEmpty() ? 0 : all.stream().mapToDouble(Review::getRating).average().orElse(0.0);
        return new ReviewSummaryResponse(average, all.size(), responses);
    }
}
