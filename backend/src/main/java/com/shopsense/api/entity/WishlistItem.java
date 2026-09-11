package com.shopsense.api.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "wishlist_items", uniqueConstraints = @UniqueConstraint(name = "uk_wishlist_product", columnNames = {"wishlist_id", "product_id"}))
public class WishlistItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wishlist_id", nullable = false)
    private Wishlist wishlist;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public UUID getId() { return id; }
    public Wishlist getWishlist() { return wishlist; }
    public void setWishlist(Wishlist wishlist) { this.wishlist = wishlist; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}
