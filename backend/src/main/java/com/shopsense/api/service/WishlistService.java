package com.shopsense.api.service;

import com.shopsense.api.dto.WishlistDtos.*;
import com.shopsense.api.entity.*;
import com.shopsense.api.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.UUID;

@Service
@Transactional
public class WishlistService {
    private final WishlistRepository wishlists;
    private final WishlistItemRepository items;
    private final UserRepository users;
    private final ProductRepository products;

    public WishlistService(WishlistRepository wishlists, WishlistItemRepository items, UserRepository users, ProductRepository products) {
        this.wishlists = wishlists;
        this.items = items;
        this.users = users;
        this.products = products;
    }

    public WishlistResponse get(String email) {
        Wishlist wishlist = wishlist(email);
        return new WishlistResponse(wishlist.getItems().stream()
            .sorted(Comparator.comparing(item -> item.getProduct().getName().toLowerCase()))
            .map(item -> new WishlistItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getSlug(),
                item.getProduct().getImageUrl(),
                item.getProduct().getBrand(),
                item.getProduct().getDiscountPrice() != null ? item.getProduct().getDiscountPrice().toPlainString() : item.getProduct().getPrice().toPlainString()
            )).toList(), wishlist.getItems().size());
    }

    public WishlistResponse add(String email, AddWishlistItemRequest request) {
        Product product = products.findById(request.productId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        Wishlist wishlist = wishlist(email);
        boolean exists = wishlist.getItems().stream().anyMatch(item -> item.getProduct().getId().equals(product.getId()));
        if (!exists) {
            WishlistItem item = new WishlistItem();
            item.setWishlist(wishlist);
            item.setProduct(product);
            wishlist.getItems().add(item);
            wishlists.save(wishlist);
        }
        return get(email);
    }

    public void remove(String email, UUID productId) {
        Wishlist wishlist = wishlist(email);
        wishlist.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        wishlists.save(wishlist);
    }

    private Wishlist wishlist(String email) {
        return wishlists.findByUserEmailIgnoreCase(email).orElseGet(() -> {
            User user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
            Wishlist created = new Wishlist();
            created.setUser(user);
            return wishlists.save(created);
        });
    }
}
