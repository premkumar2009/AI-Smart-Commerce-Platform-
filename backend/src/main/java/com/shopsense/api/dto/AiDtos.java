package com.shopsense.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class AiDtos {
    private AiDtos() {}
    public record SearchRequest(@NotBlank String query) {}
    public record SearchCriteria(String category, String productType, String brand, BigDecimal maxPrice, BigDecimal minPrice, BigDecimal minRating, List<String> keywords) {
        public SearchCriteria(String category, BigDecimal maxPrice, BigDecimal minPrice, BigDecimal minRating, List<String> keywords) { this(category, null, null, maxPrice, minPrice, minRating, keywords); }
    }
    public record SearchResponse(SearchCriteria criteria, List<ProductDtos.ProductResponse> products, String explanation, boolean aiEnhanced) {}
    public record AssistantRequest(@NotBlank String query) {}
    public record AssistantResponse(String answer, List<ProductDtos.ProductResponse> products, List<String> suggestedQueries) {}
    public record RecommendationRequest(String category, String query, UUID productId, @Positive Integer limit) {}
    public record RecommendationResponse(List<ProductDtos.ProductResponse> products, String explanation) {}
    public record SimilarRequest(@NotNull UUID productId, @Positive Integer limit) {}
    public record SimilarResponse(List<ProductDtos.ProductResponse> products, String explanation) {}
    public record CompareRequest(List<UUID> productIds) {}
    public record CompareResponse(UUID bestProductId, UUID bestValueId, String summary, List<ProductDtos.ProductResponse> products) {}
    public record BudgetRequest(@NotBlank String query, @Positive Integer limit) {}
    public record BudgetResponse(List<ProductDtos.ProductResponse> products, BigDecimal total, BigDecimal budget, String explanation) {}
}
