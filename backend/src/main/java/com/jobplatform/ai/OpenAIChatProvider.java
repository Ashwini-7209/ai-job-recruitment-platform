package com.jobplatform.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Component
public class OpenAIChatProvider implements ChatAIProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAIChatProvider.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final int maxTokens;
    private final boolean available;

    public OpenAIChatProvider(
            @Value("${app.ai.api-key:}") String apiKey,
            @Value("${app.ai.model:gpt-4o-mini}") String model,
            @Value("${app.ai.max-tokens:4000}") int maxTokens) {
        this.model = model;
        this.maxTokens = maxTokens;
        this.objectMapper = new ObjectMapper();
        this.available = apiKey != null && !apiKey.isBlank();

        if (this.available) {
            this.restClient = RestClient.builder()
                    .baseUrl("https://api.openai.com/v1")
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
        } else {
            this.restClient = null;
            log.info("AI provider not configured - using deterministic fallbacks only");
        }
    }

    @Override
    public Optional<String> chat(String systemPrompt, String userPrompt, int maxTokens) {
        if (!available || restClient == null) {
            return Optional.empty();
        }
        try {
            String requestBody = objectMapper.writeValueAsString(new ChatRequest(
                    model,
                    List.of(new Message("user", userPrompt)),
                    maxTokens
            ));

            String response = restClient.post()
                    .uri("/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").get(0).path("message").path("content").asText();
            return Optional.ofNullable(content);
        } catch (Exception e) {
            log.warn("AI chat request failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public String getProviderName() {
        return "openai";
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    public String getModel() {
        return model;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    private record ChatRequest(String model, List<Message> messages, int maxTokens) {}
    private record Message(String role, String content) {}
}
