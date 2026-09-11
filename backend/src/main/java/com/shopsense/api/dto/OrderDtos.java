package com.shopsense.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class OrderDtos {
    private OrderDtos() {}

    public record ShippingDetails(
        @NotBlank String fullName,
        @NotBlank @Pattern(regexp = "^[0-9+()\\-\\s]{7,20}$") String phone,
        @NotBlank String address,
        @NotBlank String city,
        @NotBlank String state,
        @NotBlank String postalCode,
        @NotBlank String country
    ) {}

    public record CreateOrderRequest(@NotNull ShippingDetails shipping) {}

    public record OrderItemResponse(UUID id, UUID productId, String productName, String imageUrl, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {}

    public record OrderResponse(UUID id, String status, Instant createdAt, BigDecimal subtotal, BigDecimal shipping, BigDecimal total, int itemCount, ShippingDetails shippingDetails, List<OrderItemResponse> items) {}
}
