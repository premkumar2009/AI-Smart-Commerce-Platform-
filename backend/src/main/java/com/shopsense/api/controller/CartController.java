package com.shopsense.api.controller;

import com.shopsense.api.dto.CartDtos.*;
import com.shopsense.api.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService service;
    public CartController(CartService service) { this.service = service; }
    @GetMapping public CartResponse get(Authentication authentication) { return service.get(authentication.getName()); }
    @PostMapping("/items") public CartResponse add(Authentication authentication, @Valid @RequestBody AddItemRequest request) { return service.add(authentication.getName(), request); }
    @PutMapping("/items/{id}") public CartResponse update(Authentication authentication, @PathVariable UUID id, @Valid @RequestBody UpdateItemRequest request) { return service.update(authentication.getName(), id, request); }
    @DeleteMapping("/items/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void remove(Authentication authentication, @PathVariable UUID id) { service.remove(authentication.getName(), id); }
    @DeleteMapping @ResponseStatus(HttpStatus.NO_CONTENT) public void clear(Authentication authentication) { service.clear(authentication.getName()); }
}
