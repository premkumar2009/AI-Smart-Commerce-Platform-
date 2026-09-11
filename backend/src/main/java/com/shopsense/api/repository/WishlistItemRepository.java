package com.shopsense.api.repository;

import com.shopsense.api.entity.Product;
import com.shopsense.api.entity.Wishlist;
import com.shopsense.api.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, UUID> {
    Optional<WishlistItem> findByWishlistAndProduct(Wishlist wishlist, Product product);
}
