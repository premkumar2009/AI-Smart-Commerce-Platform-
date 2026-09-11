package com.shopsense.api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public final class ProductDtos {
    private ProductDtos() {}
    public record ProductResponse(UUID id, String name, String slug, String brand, String description, String shortDescription, BigDecimal price, BigDecimal discountPrice, UUID categoryId, String categoryName, String productType, BigDecimal rating, int reviewCount, String sku, int stock, String material, String color, String tags, String imageUrl) {}
    public record ProductCreateRequest(@NotBlank @Size(max = 180) String name, @NotBlank @Size(max = 220) String slug, @NotBlank String brand, @NotBlank String description, @NotBlank String shortDescription, @NotNull @Positive BigDecimal price, @Positive BigDecimal discountPrice, @NotNull UUID categoryId, @NotBlank String productType, @NotBlank String sku, @PositiveOrZero int stock, String material, String color, String tags, String imageUrl) {}
    public record ProductUpdateRequest(@NotBlank String name, @NotBlank String description, @NotBlank String shortDescription, @NotNull @Positive BigDecimal price, @Positive BigDecimal discountPrice, @NotNull UUID categoryId, @NotBlank String productType, @PositiveOrZero int stock, String material, String color, String tags, String imageUrl) {}
}
