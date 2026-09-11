package com.shopsense.api.service;

import com.shopsense.api.dto.ProductDtos.*;
import com.shopsense.api.entity.Category;
import com.shopsense.api.entity.Product;
import com.shopsense.api.repository.CategoryRepository;
import com.shopsense.api.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {
    private final ProductRepository products;
    private final CategoryRepository categories;
    public ProductService(ProductRepository products, CategoryRepository categories) { this.products = products; this.categories = categories; }

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String search, String category, String productType, String brand, BigDecimal minPrice, BigDecimal maxPrice, BigDecimal minRating, boolean inStock, boolean outOfStock, Pageable pageable) {
        Specification<Product> specification = (root, query, cb) -> cb.conjunction();
        if (search != null && !search.isBlank()) {
            String[] terms = search.trim().toLowerCase().split("\\s+");
            for (String token : terms) {
                if (token.length() < 2) continue;
                String term = "%" + token + "%";
                specification = specification.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), term),
                    cb.like(cb.lower(root.get("brand")), term),
                    cb.like(cb.lower(root.get("description")), term),
                    cb.like(cb.lower(root.get("shortDescription")), term),
                    cb.like(cb.lower(root.get("tags")), term),
                    cb.like(cb.lower(root.get("material")), term),
                    cb.like(cb.lower(root.get("color")), term),
                    cb.like(cb.lower(root.get("productType")), term),
                    cb.like(cb.lower(root.get("sku")), term),
                    cb.like(cb.lower(root.join("category").get("name")), term),
                    cb.like(cb.lower(root.join("category").get("slug")), term)
                ));
            }
        }
        if (category != null && !category.isBlank()) specification = specification.and((root, query, cb) -> cb.equal(cb.lower(root.join("category").get("slug")), category.trim().toLowerCase()));
        if (productType != null && !productType.isBlank()) specification = specification.and((root, query, cb) -> cb.equal(cb.lower(root.get("productType")), productType.trim().toLowerCase().replace('-', ' ')));
        if (brand != null && !brand.isBlank()) specification = specification.and((root, query, cb) -> cb.equal(cb.lower(root.get("brand")), brand.trim().toLowerCase()));
        if (minPrice != null) specification = specification.and((root, query, cb) -> cb.greaterThanOrEqualTo(cb.coalesce(root.get("discountPrice"), root.get("price")), minPrice));
        if (maxPrice != null) specification = specification.and((root, query, cb) -> cb.lessThanOrEqualTo(cb.coalesce(root.get("discountPrice"), root.get("price")), maxPrice));
        if (minRating != null) specification = specification.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("rating"), minRating));
        if (inStock && !outOfStock) specification = specification.and((root, query, cb) -> cb.greaterThan(root.get("stock"), 0));
        if (outOfStock && !inStock) specification = specification.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("stock"), 0));
        return products.findAll(specification, pageable).map(this::toResponse);
    }
    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String search, String category, String brand, BigDecimal minPrice, BigDecimal maxPrice, BigDecimal minRating, boolean inStock, boolean outOfStock, Pageable pageable) {
        return search(search, category, null, brand, minPrice, maxPrice, minRating, inStock, outOfStock, pageable);
    }
    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id) { return toResponse(products.findById(id).orElseThrow(() -> notFound("Product not found"))); }
    @Transactional(readOnly = true)
    public ProductResponse findBySlug(String slug) { return toResponse(products.findBySlug(slug).orElseThrow(() -> notFound("Product not found"))); }
    @Transactional(readOnly = true)
    public ProductResponse findExactByName(String name) {
        if (name == null || name.isBlank()) return null;
        return search(name.trim(), null, null, null, null, null, null, false, false, PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "name")))
            .getContent().stream()
            .filter(product -> product.name().equalsIgnoreCase(name.trim()))
            .findFirst()
            .orElse(null);
    }
    public List<String> brands() { return products.findDistinctBrands(); }
    @Transactional(readOnly = true)
    public List<String> brands(String category, String productType) {
        String normalizedType = productType == null || productType.isBlank() ? null : productType.trim().toLowerCase().replace('-', ' ');
        return products.findDistinctBrandsByCategoryAndType(category, normalizedType);
    }
    @Transactional(readOnly = true)
    public List<TypeResponse> types(String category) {
        return products.findTypesByCategory(category).stream().map(row -> {
            String type = (String) row[0];
                List<String> distinctImages = products.findAllByCategory_SlugIgnoreCaseAndProductTypeIgnoreCase(category, type).stream()
                    .map(Product::getImageUrl)
                    .filter(image -> image != null && !image.isBlank())
                    .distinct()
                    .toList();
                if (distinctImages.size() < 3) throw new IllegalStateException("Product type " + type + " must have at least 3 distinct preview images");
                return new TypeResponse(type, ((Number) row[1]).longValue(), distinctImages.subList(0, 3));
        }).toList();
    }

    private List<String> padImages(List<String> images) { if (images.isEmpty()) return images; List<String> padded = new java.util.ArrayList<>(images); while (padded.size() < 3) padded.add(padded.get(padded.size() - 1)); return padded; }

    private List<String> representativeImages(String type) {
        String key = type.toLowerCase(java.util.Locale.ROOT);
        Map<String, List<String>> images = Map.ofEntries(
            Map.entry("headphones", List.of("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1484704849700-f032a568e944?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1546435770-a3e426bf472b?auto=format&fit=crop&w=700&q=85")),
            Map.entry("earbuds", List.of("https://images.unsplash.com/photo-1606220945770-b5b6c2c55bf1?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?auto=format&fit=crop&w=700&q=85")),
            Map.entry("keyboards", List.of("https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1595225476474-87563907a212?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1541140532154-b024d705b90a?auto=format&fit=crop&w=700&q=85")),
            Map.entry("mice", List.of("https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1563297007-0686b7003af7?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?auto=format&fit=crop&w=700&q=85")),
            Map.entry("cameras", List.of("https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1452780212940-6f5c0d14d848?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1606986628253-7a2c7c5f4f0b?auto=format&fit=crop&w=700&q=85")),
            Map.entry("monitors", List.of("https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1551645120-d70bfe84c826?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1547082299-de196ea013d6?auto=format&fit=crop&w=700&q=85")),
            Map.entry("smartwatches", List.of("https://images.unsplash.com/photo-1546868871-7041f2a55e12?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1579586337278-3befd40fd17a?auto=format&fit=crop&w=700&q=85")),
            Map.entry("speakers", List.of("https://images.unsplash.com/photo-1589003077984-894e133dabab?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1545454675-3531b543be5d?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?auto=format&fit=crop&w=700&q=85")),
            Map.entry("cookware", List.of("https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1556910103-1c02745bae4d?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1584990347449-ae7f3f2c4c99?auto=format&fit=crop&w=700&q=85")),
            Map.entry("coffee", List.of("https://images.unsplash.com/photo-1447933601403-0c6688de566e?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&w=700&q=85")),
            Map.entry("fashion", List.of("https://images.unsplash.com/photo-1496747611176-843222e1e57c?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1485230895905-ec40ba36b9bc?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=700&q=85")),
            Map.entry("shoes", List.of("https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1552674605-db6ffd4facb5?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=700&q=85")),
            Map.entry("beauty", List.of("https://images.unsplash.com/photo-1596462502278-27bfdc403348?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1571781926291-c477ebfd024b?auto=format&fit=crop&w=700&q=85")),
            Map.entry("backpack", List.of("https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1551632811-561732d1e306?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1491637639811-60e2756cc1c7?auto=format&fit=crop&w=700&q=85")),
            Map.entry("furniture", List.of("https://images.unsplash.com/photo-1518455027359-f3f8164ba6b0?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&w=700&q=700&q=85", "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&w=700&q=85")),
            Map.entry("gaming", List.of("https://images.unsplash.com/photo-1593305841991-05c297ba4575?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1547394765-185e1e68f34e?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1603481546238-487240415921?auto=format&fit=crop&w=700&q=85")),
            Map.entry("stationery", List.of("https://images.unsplash.com/photo-1517842645767-c639042777db?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1455390582262-044cdead277a?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1499951360447-b19be8fe80f5?auto=format&fit=crop&w=700&q=85")),
            Map.entry("automotive", List.of("https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?auto=format&fit=crop&w=700&q=85")),
            Map.entry("pet", List.of("https://images.unsplash.com/photo-1583337130417-3346a1be7dee?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?auto=format&fit=crop&w=700&q=85", "https://images.unsplash.com/photo-1601758228041-f3b2795255f1?auto=format&fit=crop&w=700&q=85"))
        );
        return images.entrySet().stream().filter(entry -> key.contains(entry.getKey())).map(Map.Entry::getValue).findFirst().orElse(List.of());
    }
    public ProductResponse create(ProductCreateRequest request) { Product product = new Product(); apply(product, request); return toResponse(products.save(product)); }
    public ProductResponse update(UUID id, ProductUpdateRequest request) { Product product = products.findById(id).orElseThrow(() -> notFound("Product not found")); apply(product, request); return toResponse(products.save(product)); }
    public void delete(UUID id) { if (!products.existsById(id)) throw notFound("Product not found"); products.deleteById(id); }
    private void apply(Product product, ProductCreateRequest request) { product.setName(request.name()); product.setSlug(request.slug()); product.setBrand(request.brand()); product.setDescription(request.description()); product.setShortDescription(request.shortDescription()); product.setPrice(request.price()); product.setDiscountPrice(request.discountPrice()); product.setCategory(category(request.categoryId())); product.setProductType(request.productType()); product.setSku(request.sku()); product.setStock(request.stock()); product.setMaterial(request.material()); product.setColor(request.color()); product.setTags(request.tags()); product.setImageUrl(request.imageUrl()); }
    private void apply(Product product, ProductUpdateRequest request) { product.setName(request.name()); product.setDescription(request.description()); product.setShortDescription(request.shortDescription()); product.setPrice(request.price()); product.setDiscountPrice(request.discountPrice()); product.setCategory(category(request.categoryId())); product.setProductType(request.productType()); product.setStock(request.stock()); product.setMaterial(request.material()); product.setColor(request.color()); product.setTags(request.tags()); product.setImageUrl(request.imageUrl()); }
    private Category category(UUID id) { return categories.findById(id).orElseThrow(() -> notFound("Category not found")); }
    private ProductResponse toResponse(Product p) { return new ProductResponse(p.getId(), p.getName(), p.getSlug(), p.getBrand(), p.getDescription(), p.getShortDescription(), p.getPrice(), p.getDiscountPrice(), p.getCategory().getId(), p.getCategory().getName(), p.getProductType(), p.getRating(), p.getReviewCount(), p.getSku(), p.getStock(), p.getMaterial(), p.getColor(), p.getTags(), p.getImageUrl()); }
    private ResponseStatusException notFound(String message) { return new ResponseStatusException(HttpStatus.NOT_FOUND, message); }
    public record TypeResponse(String name, long productCount, List<String> images) {}
}
