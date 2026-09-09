package com.shopsense.api.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "categories", uniqueConstraints = @UniqueConstraint(name = "uk_categories_slug", columnNames = "slug"))
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, length = 100) private String name;
    @Column(nullable = false, length = 120) private String slug;
    @Column(length = 500) private String description;
    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
