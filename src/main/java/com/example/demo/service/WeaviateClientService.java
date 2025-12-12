package com.example.demo.service;

import com.example.demo.config.WeaviateProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeaviateClientService {

    // Distance threshold
    private static final float MAX_DISTANCE = 0.5f;

    private final WeaviateProperties props;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public record EvidenceChunk(String title, String content, String source) {}

    private HttpRequest.Builder requestBuilder(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(props.getBaseUrl() + path))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json");

        String apiKey = props.getApiKey();
        if (apiKey != null && !apiKey.isBlank()) {
            builder.header("X-API-KEY", apiKey);
        }
        return builder;
    }

    public String insertArticleChunk(String title,
                                     String content,
                                     String source,
                                     float[] vector) {
        throw new UnsupportedOperationException("Use the ingestion service to write chunks to Weaviate");
    }

    public String searchByVector(float[] vector, int limit) throws Exception {
        String gql = buildQuery(vector, limit);

        ObjectNode root = mapper.createObjectNode();
        root.put("query", gql);
        String body = mapper.writeValueAsString(root);

        HttpRequest request = requestBuilder("/v1/graphql")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            log.error("Weaviate searchByVector failed status={} body={}",
                    resp.statusCode(), resp.body());
            throw new IllegalStateException("Weaviate GraphQL HTTP " + resp.statusCode());
        }

        log.debug("Weaviate searchByVector response={}", resp.body());
        return resp.body();
    }

    /**
     * Build GraphQL query using the collector's ArticleChunk schema.
     * request these fields:
     *  - text
     *  - articleTitle
     *  - sourceName
     *  - _additional { distance }
     */
    private static String buildQuery(float[] vector, int limit) {
        StringBuilder vecBuilder = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) vecBuilder.append(",");
            vecBuilder.append(vector[i]);
        }
        vecBuilder.append("]");

        return String.format(
                java.util.Locale.US,
                "{ Get { ArticleChunk(" +
                        "nearVector: { vector: %s, distance: %f }, limit: %d" +
                        ") {" +
                        " text" +
                        " articleTitle" +
                        " sourceName" +
                        " _additional { distance }" +
                        " } } }",
                vecBuilder,
                MAX_DISTANCE,
                limit
        );
    }

    public List<EvidenceChunk> parseEvidenceChunks(String graphqlResponse) throws Exception {
        JsonNode root = mapper.readTree(graphqlResponse);

        JsonNode errors = root.path("errors");
        if (errors.isArray() && !errors.isEmpty()) {
            log.error("Weaviate GraphQL errors={}", errors);
            throw new RuntimeException("Weaviate GraphQL returned errors");
        }

        JsonNode data = root.path("data").path("Get").path("ArticleChunk");
        List<EvidenceChunk> chunks = new ArrayList<>();

        if (data.isArray()) {
            for (JsonNode n : data) {
                JsonNode distNode = n.path("_additional").path("distance");
                float distance = distNode.isMissingNode() ? 1.0f : (float) distNode.asDouble();

                if (distance > MAX_DISTANCE) {
                    continue;
                }

                String title   = n.path("articleTitle").asText("");
                String content = n.path("text").asText("");
                String source  = n.path("sourceName").asText("");

                chunks.add(new EvidenceChunk(title, content, source));
            }
        } else {
            log.warn("No ArticleChunk array in Weaviate response");
        }

        log.info("parseEvidenceChunks() extracted {} chunks", chunks.size());
        return chunks;
    }
}