package com.example.demo.service;

import com.example.demo.entity.Article;
import com.example.demo.util.PromptLoader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VertexAiService {

    private final VertexAuthHelper authHelper;
    private final VertexApiClient vertexApiClient;
    private final ObjectMapper mapper;
    private final PromptLoader promptLoader;

    public VertexAiService(VertexAuthHelper authHelper,
                           VertexApiClient vertexApiClient,
                           PromptLoader promptLoader) {
        this.authHelper = authHelper;
        this.vertexApiClient = vertexApiClient;
        this.promptLoader = promptLoader;
        this.mapper = new ObjectMapper();
    }

    public String askModel(String claim, List<Article> evidence) {
        try {
            log.info("VertexAiService.askModel() called, claim length={}", claim.length());
            if (evidence != null) {
                log.info("Evidence size={}", evidence.size());
            }

            if (evidence == null) {
                evidence = List.of();
            }

            String endpoint = authHelper.chatEndpoint();
            String prompt = buildPrompt(claim, evidence);
            log.debug("Prompt to Vertex (truncated)={}...",
                    prompt.substring(0, Math.min(prompt.length(), 500)));

            String requestBody = buildRequestBody(prompt);

            HttpResponse<String> response =
                    vertexApiClient.postJson(endpoint, requestBody);

            log.info("Vertex response status={}", response.statusCode());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return extractTextFromResponse(response.body());
            } else {
                return "Vertex AI error " + response.statusCode() + ": " + response.body();
            }

        } catch (Exception e) {
            log.error("Error calling Vertex AI", e);
            return "Error calling Vertex AI: " + e.getMessage();
        }
    }

    private String buildPrompt(String claim, List<Article> evidence) {
        String basePrompt = promptLoader.loadPrompt();

        String evidenceText = evidence == null || evidence.isEmpty()
                ? "(no evidence found)"
                : evidence.stream()
                .map(a -> a.getTitle() + " — " + a.getContent())
                .collect(Collectors.joining("\n\n---\n\n"));

        return basePrompt
                + "\n\nClaim:\n" + claim
                + "\n\nEvidence:\n" + evidenceText;
    }

    private String buildRequestBody(String prompt) throws Exception {
        var root = mapper.createObjectNode();
        var contents = root.putArray("contents");

        var userContent = contents.addObject();
        userContent.put("role", "user");
        var parts = userContent.putArray("parts");
        var part = parts.addObject();
        part.put("text", prompt);

        return mapper.writeValueAsString(root);
    }

    private String extractTextFromResponse(String body) throws Exception {
        JsonNode root = mapper.readTree(body);
        JsonNode candidates = root.path("candidates");
        if (candidates.isArray() && !candidates.isEmpty()) {
            JsonNode parts = candidates.get(0)
                    .path("content")
                    .path("parts");
            if (parts.isArray() && !parts.isEmpty()) {
                JsonNode textNode = parts.get(0).path("text");
                if (!textNode.isMissingNode()) {
                    return textNode.asText();
                }
            }
        }
        return "No text field found in AI response: " + body;
    }
}