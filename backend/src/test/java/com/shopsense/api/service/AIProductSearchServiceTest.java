package com.shopsense.api.service;

import com.shopsense.api.dto.AiDtos.SearchCriteria;
import com.shopsense.api.dto.ProductDtos.ProductResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AIProductSearchServiceTest {

    @Test
    void search_whenQueryIsNull_shouldNotThrow() {
        ProductService products = mock(ProductService.class);
        GeminiService gemini = mock(GeminiService.class);
        AIProductSearchService service = new AIProductSearchService(products, gemini);

        when(products.search(any(), any(), any(), any(), any(), any(), any(Boolean.class), any(Boolean.class), any(Pageable.class)))
            .thenReturn(Page.empty());

        assertDoesNotThrow(() -> service.search(null));
    }

    @Test
    void search_shouldUseExtractedKeywordsInDatabaseQuery() {
        ProductService products = mock(ProductService.class);
        GeminiService gemini = mock(GeminiService.class);
        AIProductSearchService service = new AIProductSearchService(products, gemini);

        when(gemini.extractCriteria("wireless headphones under 5000"))
            .thenReturn(Optional.of(new SearchCriteria("electronics", new BigDecimal("5000"), null, new BigDecimal("4"), List.of("wireless", "headphones"))));
        when(products.search(any(), any(), any(), any(), any(), any(), any(Boolean.class), any(Boolean.class), any(Pageable.class)))
            .thenReturn(Page.empty());

        service.search("wireless headphones under 5000");

        ArgumentCaptor<String> searchCaptor = ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(products, org.mockito.Mockito.atLeastOnce()).search(searchCaptor.capture(), any(), any(), any(), any(), any(), any(Boolean.class), any(Boolean.class), any(Pageable.class));
        assertEquals("wireless headphones", searchCaptor.getAllValues().getFirst());
    }

    @Test
    void geminiResponseSanitizer_shouldHandleCodeBlocksAndSingleQuotedJson() {
        String sanitized = GeminiService.sanitizeResponseJson("```json\n{ 'category': 'electronics', 'maxPrice': 5000, 'keywords': ['wireless', 'headphones'] }\n```");

        assertTrue(sanitized.contains("\"category\""));
        assertTrue(sanitized.contains("\"electronics\""));
        assertTrue(sanitized.contains("\"maxPrice\""));
        assertTrue(sanitized.contains("\"keywords\""));
        assertDoesNotThrow(() -> new com.fasterxml.jackson.databind.ObjectMapper().readTree(sanitized));
    }
}
