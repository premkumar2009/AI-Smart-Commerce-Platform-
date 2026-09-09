package com.shopsense.api.service;

import com.shopsense.api.dto.ProductDtos.*;
import com.shopsense.api.entity.Category;
import com.shopsense.api.entity.Product;
import com.shopsense.api.repository.CategoryRepository;
import com.shopsense.api.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository products;
    private final CategoryRepository categories;
    public ProductService(ProductRepository products, CategoryRepository categories) { this.products = products; this.categories = categories; }

    public Page<ProductResponse> search(String search, String category, BigDecimal minPrice, BigDecimal maxPrice, BigDecimal minRating, Pageable pageable) {
        Specification<Product> specification = Specification.where(null);
        if (search != null && !search.isBlank()) { String term = "%" + search.trim().toLowerCase() + "%"; specification = specification.and((root, query, cb) -> cb.or(cb.like(cb.lower(root.get("name")), term), cb.like(cb.lower(root.get("brand")), term), cb.like(cb.lower(root.get("tags")), term))); }
        if (category != null && !category.isBlank()) specification = specification.and((root, query, cb) -> cb.equal(cb.lower(root.join("category").get("slug")), category.trim().toLowerCase()));
        if (minPrice != null) specification = specification.and((root, query, cb) -> cb.greaterThanOrEqualTo(cb.coalesce(root.get("discountPrice"), root.get("price")), minPrice));
        if (maxPrice != null) specification = specification.and((root, query, cb) -> cb.lessThanOrEqualTo(cb.coalesce(root.get("discountPrice"), root.get("price")), maxPrice));
        if (minRating != null) specification = specification.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("rating"), minRating));
        return products.findAll(specification, pageable).map(this::toResponse);
    }
    public ProductResponse findById(UUID id) { return toResponse(products.findById(id).orElseThrow(() -> notFound("Product not found"))); }
    public ProductResponse findBySlug(String slug) { return toResponse(products.findBySlug(slug).orElseThrow(() -> notFound("Product not found"))); }
    public ProductResponse create(ProductCreateRequest request) { Product product = new Product(); apply(product, request); return toResponse(products.save(product)); }
    public ProductResponse update(UUID id, ProductUpdateRequest request) { Product product = products.findById(id).orElseThrow(() -> notFound("Product not found")); apply(product, request); return toResponse(products.save(product)); }
    public void delete(UUID id) { if (!products.existsById(id)) throw notFound("Product not found"); products.deleteById(id); }
    private void apply(Product product, ProductCreateRequest request) { product.setName(request.name()); product.setSlug(request.slug()); product.setBrand(request.brand()); product.setDescription(request.description()); product.setShortDescription(request.shortDescription()); product.setPrice(request.price()); product.setDiscountPrice(request.discountPrice()); product.setCategory(category(request.categoryId())); product.setSku(request.sku()); product.setStock(request.stock()); product.setMaterial(request.material()); product.setColor(request.color()); product.setTags(request.tags()); product.setImageUrl(request.imageUrl()); }
    private void apply(Product product, ProductUpdateRequest request) { product.setName(request.name()); product.setDescription(request.description()); product.setShortDescription(request.shortDescription()); product.setPrice(request.price()); product.setDiscountPrice(request.discountPrice()); product.setCategory(category(request.categoryId())); product.setStock(request.stock()); product.setMaterial(request.material()); product.setColor(request.color()); product.setTags(request.tags()); product.setImageUrl(request.imageUrl()); }
    private Category category(UUID id) { return categories.findById(id).orElseThrow(() -> notFound("Category not found")); }
    private ProductResponse toResponse(Product p) { return new ProductResponse(p.getId(), p.getName(), p.getSlug(), p.getBrand(), p.getDescription(), p.getShortDescription(), p.getPrice(), p.getDiscountPrice(), p.getCategory().getId(), p.getCategory().getName(), p.getRating(), p.getReviewCount(), p.getSku(), p.getStock(), p.getMaterial(), p.getColor(), p.getTags(), p.getImageUrl()); }
    private ResponseStatusException notFound(String message) { return new ResponseStatusException(HttpStatus.NOT_FOUND, message); }
}
