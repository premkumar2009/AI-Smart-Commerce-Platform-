package com.shopsense.api.repository;

import com.shopsense.api.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {
    Optional<Wishlist> findByUserEmailIgnoreCase(String email);
}
