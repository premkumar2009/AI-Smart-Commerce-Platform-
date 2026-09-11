package com.shopsense.api.repository;

import com.shopsense.api.entity.Product;
import com.shopsense.api.entity.Review;
import com.shopsense.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByProductOrderByCreatedAtDesc(Product product);
    Optional<Review> findByProductAndUser(Product product, User user);
}
