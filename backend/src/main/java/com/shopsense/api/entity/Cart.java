package com.shopsense.api.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts", uniqueConstraints = @UniqueConstraint(name = "uk_carts_user", columnNames = "user_id"))
public class Cart {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true) private List<CartItem> items = new ArrayList<>();
    @Column(nullable = false) private Instant updatedAt;
    @PrePersist @PreUpdate void touch() { updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public User getUser() { return user; } public void setUser(User value) { user = value; }
    public List<CartItem> getItems() { return items; }
}
