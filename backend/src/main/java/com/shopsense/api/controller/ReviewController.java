package com.shopsense.api.controller;

import com.shopsense.api.dto.ReviewDtos.*;
import com.shopsense.api.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ReviewController {
    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    @PostMapping("/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(Authentication authentication, @Valid @RequestBody CreateReviewRequest request) {
        return service.create(authentication.getName(), request);
    }

    @GetMapping("/products/{productId}/reviews")
    public ReviewSummaryResponse getForProduct(@PathVariable UUID productId) {
        return service.getForProduct(productId);
    }
}
