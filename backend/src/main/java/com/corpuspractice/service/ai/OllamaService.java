package com.corpuspractice.service.ai;

import com.corpuspractice.config.CryptoUtil;
import com.corpuspractice.dto.*;
import com.corpuspractice.entity.AiProvider;
import com.corpuspractice.mapper.AiProviderMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Slf4j
public class OllamaService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OllamaService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    public String chat(String baseUrl, String model, String prompt) {
        try {
            String url = baseUrl;
            if (!url.endsWith("/")) url += "/";
            url += "api/chat";

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", List.of(Map.of("role", "user", "content", prompt)));
            body.put("stream", false);

            String jsonBody = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            Map<String, Object> message = (Map<String, Object>) result.get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            throw new RuntimeException("Ollama 调用失败: " + e.getMessage());
        }
    }
}
