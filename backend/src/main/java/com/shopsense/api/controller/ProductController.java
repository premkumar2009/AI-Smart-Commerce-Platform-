package com.shopsense.api.controller;

import com.shopsense.api.dto.ProductDtos.*;
import com.shopsense.api.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;
    public ProductController(ProductService service) { this.service = service; }
    @GetMapping public Page<ProductResponse> search(@RequestParam(required = false) String search, @RequestParam(required = false) String category, @RequestParam(required = false) BigDecimal minPrice, @RequestParam(required = false) BigDecimal maxPrice, @RequestParam(required = false) BigDecimal minRating, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size, @RequestParam(defaultValue = "createdAt") String sort, @RequestParam(defaultValue = "desc") String direction) {
        int safeSize = Math.min(Math.max(size, 1), 50); Sort.Direction safeDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC; Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize, Sort.by(safeDirection, sort)); return service.search(search, category, minPrice, maxPrice, minRating, pageable);
    }
    @GetMapping("/{id}") public ProductResponse byId(@PathVariable UUID id) { return service.findById(id); }
    @GetMapping("/slug/{slug}") public ProductResponse bySlug(@PathVariable String slug) { return service.findBySlug(slug); }
    @PostMapping @PreAuthorize("hasRole('ADMIN')") public ProductResponse create(@Valid @RequestBody ProductCreateRequest request) { return service.create(request); }
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public ProductResponse update(@PathVariable UUID id, @Valid @RequestBody ProductUpdateRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) @PreAuthorize("hasRole('ADMIN')") public void delete(@PathVariable UUID id) { service.delete(id); }
}
