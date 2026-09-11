package com.shopsense.api.service;

import com.shopsense.api.dto.CartDtos.*;
import com.shopsense.api.entity.*;
import com.shopsense.api.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional
public class CartService {
    private final CartRepository carts; private final UserRepository users; private final ProductRepository products;
    public CartService(CartRepository carts, UserRepository users, ProductRepository products) { this.carts = carts; this.users = users; this.products = products; }
    public CartResponse get(String email) { return response(cart(email)); }
    public CartResponse add(String email, AddItemRequest request) { Cart cart = cart(email); Product product = products.findById(request.productId()).orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Product not found")); if (request.quantity() > product.getStock()) throw error(HttpStatus.CONFLICT, "Requested quantity is not available"); CartItem item = cart.getItems().stream().filter(existing -> existing.getProduct().getId().equals(product.getId())).findFirst().orElse(null); if (item == null) { item = new CartItem(); item.setCart(cart); item.setProduct(product); cart.getItems().add(item); } int quantity = item.getQuantity() + request.quantity(); if (quantity > product.getStock()) throw error(HttpStatus.CONFLICT, "Requested quantity is not available"); item.setQuantity(quantity); return response(carts.save(cart)); }
    public CartResponse update(String email, UUID itemId, UpdateItemRequest request) { Cart cart = cart(email); CartItem item = cart.getItems().stream().filter(existing -> existing.getId().equals(itemId)).findFirst().orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Cart item not found")); if (request.quantity() > item.getProduct().getStock()) throw error(HttpStatus.CONFLICT, "Requested quantity is not available"); item.setQuantity(request.quantity()); return response(carts.save(cart)); }
    public void remove(String email, UUID itemId) { Cart cart = cart(email); cart.getItems().removeIf(item -> item.getId().equals(itemId)); carts.save(cart); }
    public void clear(String email) { Cart cart = cart(email); cart.getItems().clear(); carts.save(cart); }
    private Cart cart(String email) { return carts.findByUserEmailIgnoreCase(email).orElseGet(() -> { User user = users.findByEmailIgnoreCase(email).orElseThrow(() -> error(HttpStatus.UNAUTHORIZED, "User not found")); Cart created = new Cart(); created.setUser(user); return carts.save(created); }); }
    private CartResponse response(Cart cart) { var items = cart.getItems().stream().map(item -> { BigDecimal price = item.getProduct().getDiscountPrice() != null ? item.getProduct().getDiscountPrice() : item.getProduct().getPrice(); return new CartItemResponse(item.getId(), item.getProduct().getId(), item.getProduct().getName(), item.getProduct().getImageUrl(), price, item.getQuantity(), price.multiply(BigDecimal.valueOf(item.getQuantity()))); }).toList(); BigDecimal subtotal = items.stream().map(CartItemResponse::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add); BigDecimal shipping = subtotal.compareTo(BigDecimal.valueOf(1499)) >= 0 || subtotal.signum() == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(99); return new CartResponse(items, subtotal, shipping, subtotal.add(shipping), items.stream().mapToInt(CartItemResponse::quantity).sum()); }
    private ResponseStatusException error(HttpStatus status, String message) { return new ResponseStatusException(status, message); }
}
