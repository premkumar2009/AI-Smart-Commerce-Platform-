package com.shopsense.api.controller;

import com.shopsense.api.dto.WishlistDtos.*;
import com.shopsense.api.service.WishlistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {
    private final WishlistService service;

    public WishlistController(WishlistService service) {
        this.service = service;
    }

    @GetMapping
    public WishlistResponse get(Authentication authentication) {
        return service.get(authentication.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WishlistResponse add(Authentication authentication, @Valid @RequestBody AddWishlistItemRequest request) {
        return service.add(authentication.getName(), request);
    }

    @DeleteMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(Authentication authentication, @PathVariable UUID productId) {
        service.remove(authentication.getName(), productId);
    }
}
