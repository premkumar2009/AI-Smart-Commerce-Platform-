package com.shopsense.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsense.api.dto.AiDtos.SearchCriteria;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GeminiService {
    private final RestClient client = RestClient.create();
    private final ObjectMapper mapper;
    private final String apiKey;

    public GeminiService(ObjectMapper mapper, @Value("${GEMINI_API_KEY:}") String apiKey) {
        this.mapper = mapper;
        this.apiKey = apiKey;
    }

    public Optional<SearchCriteria> extractCriteria(String query) {
        if (apiKey == null || apiKey.isBlank()) return Optional.empty();
        try {
            String prompt = "Return only valid JSON with keys category, maxPrice, minPrice, minRating, keywords. Interpret this ecommerce request, never invent a product: " + query;
            String body = client.post()
                .uri("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))))
                .retrieve()
                .body(String.class);

            if (body == null || body.isBlank()) return Optional.empty();

            JsonNode root = mapper.readTree(body);
            String text = extractText(root);
            if (text == null || text.isBlank()) return Optional.empty();

            JsonNode json = mapper.readTree(sanitizeResponseJson(text));
            var keywords = new ArrayList<String>();
            json.path("keywords").forEach(node -> {
                if (node != null && !node.isNull()) keywords.add(node.asText());
            });
            return Optional.of(new SearchCriteria(
                json.path("category").isMissingNode() || json.path("category").isNull() ? null : json.path("category").asText(null),
                decimal(json, "maxPrice"),
                decimal(json, "minPrice"),
                decimal(json, "minRating"),
                keywords
            ));
        } catch (Exception ignored) { return Optional.empty(); }
    }

    static String sanitizeResponseJson(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        cleaned = cleaned.replaceFirst("(?s)^```(?:json)?\\s*", "");
        cleaned = cleaned.replaceFirst("(?s)\\s*```$", "");
        cleaned = cleaned.replace("\r\n", "\n");
        cleaned = cleaned.replaceAll("(?<=\\{|,\\s|\\[\\s)([A-Za-z_][A-Za-z0-9_]*)\\s*:", "\"$1\":");
        cleaned = cleaned.replace("'", "\"");
        return cleaned;
    }

    private String extractText(JsonNode root) {
        JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
        if (parts.isArray()) {
            for (JsonNode part : parts) {
                String text = part.path("text").asText(null);
                if (text != null && !text.isBlank()) return text;
            }
        }
        return root.at("/candidates/0/content/parts/0/text").asText(null);
    }

    private BigDecimal decimal(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isNumber() ? value.decimalValue() : null;
    }
}
