package com.shopsense.api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products", indexes = {@Index(name = "idx_products_slug", columnList = "slug"), @Index(name = "idx_products_category", columnList = "category_id")}, uniqueConstraints = @UniqueConstraint(name = "uk_products_sku", columnNames = "sku"))
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false, length = 180) private String name;
    @Column(nullable = false, length = 220) private String slug;
    @Column(nullable = false, length = 100) private String brand;
    @Column(nullable = false, length = 3000) private String description;
    @Column(nullable = false, length = 500) private String shortDescription;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal price;
    @Column(precision = 12, scale = 2) private BigDecimal discountPrice;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "category_id", nullable = false) private Category category;
    @Column(nullable = false, precision = 3, scale = 2) private BigDecimal rating = BigDecimal.ZERO;
    @Column(nullable = false) private int reviewCount;
    @Column(nullable = false, length = 60) private String sku;
    @Column(nullable = false) private int stock;
    @Column(length = 100) private String material;
    @Column(length = 100) private String color;
    @Column(length = 1000) private String tags;
    @Column(length = 1000) private String imageUrl;
    @Column(nullable = false, updatable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;
    @PrePersist void onCreate() { createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public String getName() { return name; } public void setName(String value) { name = value; }
    public String getSlug() { return slug; } public void setSlug(String value) { slug = value; }
    public String getBrand() { return brand; } public void setBrand(String value) { brand = value; }
    public String getDescription() { return description; } public void setDescription(String value) { description = value; }
    public String getShortDescription() { return shortDescription; } public void setShortDescription(String value) { shortDescription = value; }
    public BigDecimal getPrice() { return price; } public void setPrice(BigDecimal value) { price = value; }
    public BigDecimal getDiscountPrice() { return discountPrice; } public void setDiscountPrice(BigDecimal value) { discountPrice = value; }
    public Category getCategory() { return category; } public void setCategory(Category value) { category = value; }
    public BigDecimal getRating() { return rating; } public void setRating(BigDecimal value) { rating = value; }
    public int getReviewCount() { return reviewCount; } public void setReviewCount(int value) { reviewCount = value; }
    public String getSku() { return sku; } public void setSku(String value) { sku = value; }
    public int getStock() { return stock; } public void setStock(int value) { stock = value; }
    public String getMaterial() { return material; } public void setMaterial(String value) { material = value; }
    public String getColor() { return color; } public void setColor(String value) { color = value; }
    public String getTags() { return tags; } public void setTags(String value) { tags = value; }
    public String getImageUrl() { return imageUrl; } public void setImageUrl(String value) { imageUrl = value; }
}
