package com.shopsense.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public final class ReviewDtos {
    private ReviewDtos() {}

    public record CreateReviewRequest(@NotNull UUID productId, @Min(1) @Max(5) int rating, @NotBlank String reviewText) {}

    public record ReviewResponse(UUID id, UUID productId, String userName, int rating, String reviewText, Instant createdAt) {}

    public record ReviewSummaryResponse(double averageRating, long reviewCount, java.util.List<ReviewResponse> reviews) {}
}
