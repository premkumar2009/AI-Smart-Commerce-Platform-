package com.shopsense.api.controller;

import com.shopsense.api.entity.Category;
import com.shopsense.api.repository.CategoryRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryRepository categories;
    public CategoryController(CategoryRepository categories) { this.categories = categories; }
    @GetMapping public List<Category> all() { return categories.findAll(); }
    @PostMapping @PreAuthorize("hasRole('ADMIN')") public Category create(@RequestBody Category category) { return categories.save(category); }
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public Category update(@PathVariable UUID id, @RequestBody Category input) { Category category = categories.findById(id).orElseThrow(); category.setName(input.getName()); category.setSlug(input.getSlug()); category.setDescription(input.getDescription()); return categories.save(category); }
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public void delete(@PathVariable UUID id) { categories.deleteById(id); }
}
