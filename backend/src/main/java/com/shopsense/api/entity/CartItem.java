package com.shopsense.api.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "cart_items", uniqueConstraints = @UniqueConstraint(name = "uk_cart_product", columnNames = {"cart_id", "product_id"}))
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "cart_id", nullable = false) private Cart cart;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "product_id", nullable = false) private Product product;
    @Column(nullable = false) private int quantity;
    public UUID getId() { return id; }
    public Cart getCart() { return cart; } public void setCart(Cart value) { cart = value; }
    public Product getProduct() { return product; } public void setProduct(Product value) { product = value; }
    public int getQuantity() { return quantity; } public void setQuantity(int value) { quantity = value; }
}
