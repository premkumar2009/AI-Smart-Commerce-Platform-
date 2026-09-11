package com.shopsense.api.controller;

import com.shopsense.api.dto.AiDtos.*;
import com.shopsense.api.service.AIProductSearchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {
    private final AIProductSearchService search;
    public AIController(AIProductSearchService search) { this.search = search; }

    @PostMapping("/search")
    public SearchResponse search(@Valid @RequestBody SearchRequest request) { return search.search(request.query()); }

    @PostMapping("/assistant")
    public AssistantResponse assistant(@Valid @RequestBody AssistantRequest request) { return search.assistant(request.query()); }

    @PostMapping("/recommendations")
    public RecommendationResponse recommendations(@Valid @RequestBody RecommendationRequest request) { return search.recommendations(request.category(), request.query(), request.productId(), request.limit()); }

    @PostMapping("/similar")
    public SimilarResponse similar(@Valid @RequestBody SimilarRequest request) { return search.similar(request.productId(), request.limit()); }

    @PostMapping("/compare")
    public CompareResponse compare(@Valid @RequestBody CompareRequest request) { return search.compare(request.productIds()); }

    @PostMapping("/budget")
    public BudgetResponse budget(@Valid @RequestBody BudgetRequest request) { return search.budget(request.query(), request.limit()); }
}
