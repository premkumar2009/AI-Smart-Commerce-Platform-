package com.shopsense.api.controller;

import com.shopsense.api.dto.OrderDtos.*;
import com.shopsense.api.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(Authentication authentication, @Valid @RequestBody CreateOrderRequest request) {
        return service.create(authentication.getName(), request);
    }

    @GetMapping("/orders")
    public List<OrderResponse> get(Authentication authentication) {
        return service.getOrders(authentication.getName());
    }

    @GetMapping("/orders/{id}")
    public OrderResponse getById(Authentication authentication, @PathVariable UUID id) {
        return service.getById(authentication.getName(), id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/orders")
    public List<OrderResponse> adminOrders(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities().stream().noneMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        return service.getAllOrders();
    }
}
