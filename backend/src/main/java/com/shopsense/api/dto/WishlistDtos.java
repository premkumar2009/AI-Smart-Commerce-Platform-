package com.shopsense.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public final class WishlistDtos {
    private WishlistDtos() {}

    public record AddWishlistItemRequest(@NotNull UUID productId) {}
    public record WishlistItemResponse(UUID id, UUID productId, String productName, String productSlug, String imageUrl, String brand, String price) {}
    public record WishlistResponse(List<WishlistItemResponse> items, int itemCount) {}
}
