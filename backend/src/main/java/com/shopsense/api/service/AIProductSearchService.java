package com.shopsense.api.service;

import com.shopsense.api.dto.AiDtos.*;
import com.shopsense.api.dto.ProductDtos.ProductResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AIProductSearchService {
    private final ProductService products;
    private final GeminiService gemini;

    public AIProductSearchService(ProductService products, GeminiService gemini) { this.products = products; this.gemini = gemini; }

    public SearchResponse search(String query) {
        String normalizedQuery = query == null ? "" : query.trim();
        ProductResponse catalogProduct = findCatalogProduct(normalizedQuery);
        if (catalogProduct != null) {
            SearchCriteria exactCriteria = new SearchCriteria(
                normalizeCategory(catalogProduct.categoryName()),
                catalogProduct.productType(),
                catalogProduct.brand(),
                null,
                null,
                null,
                List.of(catalogProduct.name())
            );
            return new SearchResponse(exactCriteria, List.of(catalogProduct), "I found this product in the ShopSense catalog.", false);
        }
        SearchCriteria fallback = parse(normalizedQuery);
        try {
            var aiCriteria = gemini.extractCriteria(normalizedQuery);
            SearchCriteria criteria = aiCriteria.orElse(fallback);
            String category = normalizeCategory(criteria.category());
            String productType = normalizeType(criteria.productType());
            List<String> keywords = criteria.keywords() == null ? List.of() : criteria.keywords().stream().filter(token -> token != null && !token.isBlank()).toList();
            String keywordQuery = !keywords.isEmpty() ? String.join(" ", keywords) : (normalizedQuery.isBlank() ? "" : normalizedQuery);
            var result = criteria.productType() == null && criteria.brand() == null ? products.search(keywordQuery, category, null, criteria.minPrice(), criteria.maxPrice(), criteria.minRating(), true, false, PageRequest.of(0, 12, Sort.by(Sort.Direction.DESC, "rating"))) : products.search(keywordQuery, category, productType, criteria.brand(), criteria.minPrice(), criteria.maxPrice(), criteria.minRating(), true, false, PageRequest.of(0, 12, Sort.by(Sort.Direction.DESC, "rating")));
            List<ProductResponse> matches = result == null ? List.of() : result.getContent();
            if (matches.isEmpty() && category != null) {
                matches = products.search("", category, productType, criteria.brand(), criteria.minPrice(), criteria.maxPrice(), criteria.minRating(), true, false, PageRequest.of(0, 12, Sort.by(Sort.Direction.DESC, "rating"))).getContent();
            }
            String explanation = matches.isEmpty() ? "I could not find an exact match. Try adding a category, budget, or feature." : "I found " + matches.size() + " real products from the ShopSense catalog matching " + String.join(", ", keywords.isEmpty() ? List.of(normalizedQuery.split("\\s+")) : keywords) + ".";
            return new SearchResponse(criteria, matches, explanation, aiCriteria.isPresent());
        } catch (Exception exception) {
            return fallbackSearch(normalizedQuery, fallback);
        }
    }

    private ProductResponse findCatalogProduct(String query) {
        if (query == null || query.isBlank()) return null;
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        ProductResponse exact = products.findExactByName(query);
        if (exact != null) return exact;
        var candidatePage = products.search(query, null, null, null, null, null, null, false, false, PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "rating")));
        if (candidatePage == null) return null;
        List<ProductResponse> candidates = candidatePage.getContent();
        return candidates.stream()
            .filter(product -> product.name() != null && product.name().toLowerCase(Locale.ROOT).contains(normalized))
            .findFirst()
            .orElse(null);
    }

    private SearchResponse fallbackSearch(String normalizedQuery, SearchCriteria fallback) {
        String category = normalizeCategory(fallback.category());
        List<String> keywords = fallback.keywords() == null ? List.of() : fallback.keywords().stream().filter(token -> token != null && !token.isBlank()).toList();
        String keywordQuery = !keywords.isEmpty() ? String.join(" ", keywords) : (normalizedQuery.isBlank() ? "" : normalizedQuery);
        var result = fallback.productType() == null && fallback.brand() == null ? products.search(keywordQuery, category, null, fallback.minPrice(), fallback.maxPrice(), fallback.minRating(), true, false, PageRequest.of(0, 12, Sort.by(Sort.Direction.DESC, "rating"))) : products.search(keywordQuery, category, fallback.productType(), fallback.brand(), fallback.minPrice(), fallback.maxPrice(), fallback.minRating(), true, false, PageRequest.of(0, 12, Sort.by(Sort.Direction.DESC, "rating")));
        List<ProductResponse> matches = result == null ? List.of() : result.getContent();
        if (matches.isEmpty() && category != null) {
            var categoryResult = products.search("", category, fallback.productType(), fallback.brand(), fallback.minPrice(), fallback.maxPrice(), fallback.minRating(), true, false, PageRequest.of(0, 12, Sort.by(Sort.Direction.DESC, "rating")));
            matches = categoryResult == null ? List.of() : categoryResult.getContent();
        }
        return new SearchResponse(fallback, matches, matches.isEmpty() ? "I could not find a grounded match. Try a broader keyword or budget range." : "I used the live catalog to find results for your shopping intent.", false);
    }

    public AssistantResponse assistant(String query) {
        SearchResponse searchResponse = search(query);
        List<String> queries = List.of(
            "wireless headphones under ₹5000",
            "running shoes under ₹4000",
            "travel essentials under ₹3000"
        );
        String intent = searchResponse.criteria().category() == null ? "that request" : searchResponse.criteria().category() + " products";
        String answer = searchResponse.products().isEmpty()
            ? "I could not find a grounded match for " + intent + ". Try a broader category or a more flexible budget."
            : "I found " + searchResponse.products().size() + " real catalog options for " + intent + ". " + explainTradeoffs(searchResponse.products());
        return new AssistantResponse(
            answer,
            searchResponse.products().stream().limit(5).toList(),
            queries
        );
    }

    public RecommendationResponse recommendations(String category, String query, UUID productId, Integer limit) {
        String searchText = query == null || query.isBlank() ? (category == null ? "" : category) : query;
        SearchResponse searchResponse = search(searchText);
        List<ProductResponse> products = searchResponse.products().stream()
            .filter(p -> productId == null || !p.id().equals(productId))
            .limit(limit == null || limit <= 0 ? 5 : limit)
            .toList();
        String explanation = products.isEmpty() ? "I could not build a recommendation set for that request from the current catalog." : "I matched these products to your shopping intent using real catalog data.";
        return new RecommendationResponse(products, explanation);
    }

    public SimilarResponse similar(UUID productId, Integer limit) {
        if (productId == null) return new SimilarResponse(List.of(), "No product was supplied to compare against.");
        ProductResponse base = products.findById(productId);
        String category = normalizeCategory(base.categoryName());
        List<ProductResponse> candidates = products.search("", category, null, null, null, null, true, false, PageRequest.of(0, 40, Sort.by(Sort.Direction.DESC, "rating"))).getContent();
        List<ProductResponse> matches = candidates.stream()
            .filter(candidate -> !candidate.id().equals(productId))
            .sorted(Comparator.comparingDouble(candidate -> -similarityScore(base, candidate)))
            .limit(limit == null || limit <= 0 ? 4 : limit)
            .toList();
        return new SimilarResponse(matches, "Ranked by shared category, brand, materials, colours, tags, and rating from the live catalog.");
    }

    public CompareResponse compare(List<UUID> productIds) {
        List<ProductResponse> selected = new ArrayList<>();
        if (productIds != null) {
            for (UUID id : productIds) {
                if (id != null) {
                    try { selected.add(products.findById(id)); } catch (RuntimeException ignored) { }
                }
            }
        }
        if (selected.isEmpty()) return new CompareResponse(null, null, "No products were available for comparison.", List.of());
        ProductResponse bestOverall = selected.stream().max(Comparator.comparingDouble(this::overallScore)).orElse(selected.getFirst());
        ProductResponse bestValue = selected.stream().min(Comparator.comparingDouble(this::valueScore)).orElse(selected.getFirst());
        String summary = "Best overall is " + bestOverall.name() + " for its rating and review confidence. "
            + "Best value is " + bestValue.name() + " at " + money(priceOf(bestValue)) + " with a " + bestValue.rating() + " rating. "
            + "I compared only the selected real catalog products, including current price and stock.";
        return new CompareResponse(bestOverall.id(), bestValue.id(), summary, selected);
    }

    public BudgetResponse budget(String query, Integer limit) {
        SearchResponse searchResponse = search(query == null ? "" : query);
        int maximumItems = limit == null || limit <= 0 ? 6 : limit;
        BigDecimal budget = extractBudget(query);
        List<ProductResponse> candidates = searchResponse.products().stream().filter(product -> priceOf(product) != null).toList();
        List<ProductResponse> products = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (ProductResponse product : candidates.stream().sorted(Comparator.comparingDouble(this::valueScore)).toList()) {
            BigDecimal price = priceOf(product);
            if (products.size() >= maximumItems) break;
            if (budget != null && total.add(price).compareTo(budget) > 0) continue;
            products.add(product);
            total = total.add(price);
        }
        String explanation = budget == null ? "I picked a grounded bundle from the current catalog." : "I built a grounded bundle within your budget of ₹" + budget.toPlainString() + ". The total is ₹" + total.toPlainString() + ".";
        return new BudgetResponse(products, total, budget, explanation);
    }

    private String explainTradeoffs(List<ProductResponse> matches) {
        ProductResponse topRated = matches.stream().max(Comparator.comparingDouble(item -> item.rating() == null ? 0 : item.rating().doubleValue())).orElse(matches.getFirst());
        ProductResponse lowestPrice = matches.stream().min(Comparator.comparingDouble(this::valueScore)).orElse(matches.getFirst());
        return "The strongest rating is " + topRated.name() + ", while " + lowestPrice.name() + " is the lower-cost option.";
    }

    private double overallScore(ProductResponse product) {
        double rating = product.rating() == null ? 0 : product.rating().doubleValue();
        double confidence = Math.min(product.reviewCount(), 100) / 100.0;
        double availability = product.stock() > 0 ? 1 : 0;
        return rating * 0.65 + confidence * 0.2 + availability * 0.15;
    }

    private double valueScore(ProductResponse product) {
        double rating = product.rating() == null ? 1 : Math.max(product.rating().doubleValue(), 1);
        return priceOf(product).doubleValue() / rating;
    }

    private double similarityScore(ProductResponse base, ProductResponse candidate) {
        double score = candidate.rating() == null ? 0 : candidate.rating().doubleValue() * 0.15;
        if (same(base.categoryName(), candidate.categoryName())) score += 0.35;
        if (same(base.brand(), candidate.brand())) score += 0.2;
        if (same(base.material(), candidate.material())) score += 0.1;
        if (same(base.color(), candidate.color())) score += 0.05;
        score += overlap(base.tags(), candidate.tags()) * 0.15;
        score += overlap(base.name(), candidate.name()) * 0.05;
        return score;
    }

    private BigDecimal priceOf(ProductResponse product) { return product.discountPrice() != null ? product.discountPrice() : product.price(); }
    private String money(BigDecimal value) { return "₹" + value.toPlainString(); }
    private boolean same(String left, String right) { return left != null && right != null && left.equalsIgnoreCase(right); }
    private double overlap(String left, String right) {
        if (left == null || right == null) return 0;
        List<String> leftTokens = List.of(left.toLowerCase(Locale.ROOT).split("[,\\s]+"));
        List<String> rightTokens = List.of(right.toLowerCase(Locale.ROOT).split("[,\\s]+"));
        long shared = leftTokens.stream().filter(token -> token.length() > 2 && rightTokens.contains(token)).count();
        return Math.min(1, shared / 3.0);
    }

    private BigDecimal extractBudget(String query) {
        if (query == null || query.isBlank()) return null;
        Matcher matcher = Pattern.compile("(?:under|below|within|less than|upto|up to)\\s*(?:₹|rs\\.?\\s*)?([0-9,]+)").matcher(query.toLowerCase(Locale.ROOT));
        if (!matcher.find()) return null;
        return new BigDecimal(matcher.group(1).replace(",", ""));
    }

    private String normalizeCategory(String value) {
        if (value == null) return null;
        String normalized = value.toLowerCase(Locale.ROOT);
        if (normalized.contains("run") || normalized.contains("shoe")) return "sports-fitness";
        if (normalized.contains("travel") || normalized.contains("luggage") || normalized.contains("backpack") || normalized.contains("suitcase")) return "travel";
        for (String category : List.of("electronics", "home-kitchen", "groceries", "fashion", "beauty", "sports-fitness", "travel", "furniture-decor", "gaming", "books-stationery", "automotive", "pet-supplies")) if (normalized.contains(category) || normalized.contains(category.replace('-', ' '))) return category;
        if (normalized.contains("kitchen") || normalized.contains("home and kitchen")) return "home-kitchen";
        if (normalized.contains("grocery") || normalized.contains("groceries")) return "groceries";
        if (normalized.contains("sport") || normalized.contains("fitness")) return "sports-fitness";
        if (normalized.contains("furniture") || normalized.contains("decor")) return "furniture-decor";
        if (normalized.contains("game") || normalized.contains("gaming")) return "gaming";
        if (normalized.contains("book") || normalized.contains("stationery")) return "books-stationery";
        if (normalized.contains("automotive") || normalized.contains("car")) return "automotive";
        if (normalized.contains("pet") || normalized.contains("dog") || normalized.contains("cat")) return "pet-supplies";
        return normalized;
    }

    private String normalizeType(String value) { return value == null ? null : value.trim().toLowerCase(Locale.ROOT).replace('-', ' '); }

    private SearchCriteria parse(String query) {
        String normalized = query.toLowerCase(Locale.ROOT); String category = null; String productType = null; String brand = null;
        category = normalizeCategory(normalized);
        if (category == null && (normalized.contains("shoe") || normalized.contains("jogging") || normalized.contains("running"))) category = "sports-fitness";
        if (normalized.contains("dog food") || normalized.contains("puppy food")) { category = "pet-supplies"; productType = "Dog Food"; }
        else if (normalized.contains("cat food") || normalized.contains("kitten food")) { category = "pet-supplies"; productType = "Cat Food"; }
        else if (normalized.contains("gaming") && normalized.contains("keyboard")) { category = "gaming"; productType = "Gaming Keyboards"; }
        else if (normalized.contains("gaming") && normalized.contains("mouse")) { category = "gaming"; productType = "Gaming Mice"; }
        else if (normalized.contains("backpack")) { category = "travel"; productType = "Backpacks"; }
        else if (normalized.contains("headphone")) { category = "electronics"; productType = "Headphones"; }
        else if (normalized.contains("earbud")) { category = "electronics"; productType = "Earbuds"; }
        else if (normalized.contains("smartphone") || normalized.contains("phone")) { category = "electronics"; productType = "Smartphones"; }
        else if (normalized.contains("laptop")) { category = "electronics"; productType = "Laptops"; }
        for (String candidate : List.of("sony", "bose", "jbl", "apple", "samsung", "oneplus", "dell", "hp", "lenovo", "asus", "logitech")) if (normalized.contains(candidate)) { brand = candidate; break; }
        Matcher max = Pattern.compile("(?:under|below|within|less than|upto|up to)\\s*(?:₹|rs\\.?\\s*)?([0-9,]+)").matcher(normalized);
        BigDecimal maxPrice = max.find() ? new BigDecimal(max.group(1).replace(",", "")) : null;
        Matcher min = Pattern.compile("(?:above|over|at least)\\s*(?:₹|rs\\.?\\s*)?([0-9,]+)").matcher(normalized);
        BigDecimal minPrice = min.find() ? new BigDecimal(min.group(1).replace(",", "")) : null;
        BigDecimal minRating = normalized.contains("highly rated") || normalized.contains("best") ? BigDecimal.valueOf(4) : null;
        return new SearchCriteria(category, productType, brand, maxPrice, minPrice, minRating, List.of(normalized.split("\\s+")));
    }
}
