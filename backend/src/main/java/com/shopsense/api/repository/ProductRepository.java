package com.shopsense.api.repository;

import com.shopsense.api.entity.Category;
import com.shopsense.api.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    Optional<Product> findBySlug(String slug);
    Optional<Product> findBySku(String sku);
    Optional<Product> findFirstByNameIgnoreCase(String name);
    List<Product> findByCategory(Category category);
    @Query("select distinct p.brand from Product p order by p.brand asc")
    List<String> findDistinctBrands();
    @Query("select distinct p.brand from Product p where lower(p.category.slug) = lower(:category) and (:productType is null or lower(p.productType) = lower(:productType)) order by p.brand asc")
    List<String> findDistinctBrandsByCategoryAndType(String category, String productType);
    @Query("select p.productType, count(p), min(p.imageUrl) from Product p where lower(p.category.slug) = lower(:category) group by p.productType order by p.productType asc")
    List<Object[]> findTypesByCategory(String category);
    List<Product> findTop3ByCategory_SlugIgnoreCaseAndProductTypeIgnoreCase(String category, String productType);
    List<Product> findAllByCategory_SlugIgnoreCaseAndProductTypeIgnoreCase(String category, String productType);
}
