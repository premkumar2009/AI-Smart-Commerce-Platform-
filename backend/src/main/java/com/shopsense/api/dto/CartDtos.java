package com.shopsense.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class CartDtos {
    private CartDtos() {}
    public record AddItemRequest(@NotNull UUID productId, @Min(1) int quantity) {}
    public record UpdateItemRequest(@Min(1) int quantity) {}
    public record CartItemResponse(UUID id, UUID productId, String productName, String imageUrl, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {}
    public record CartResponse(List<CartItemResponse> items, BigDecimal subtotal, BigDecimal shipping, BigDecimal total, int itemCount) {}
}
