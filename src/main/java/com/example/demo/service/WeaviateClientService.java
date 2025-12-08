package com.example.demo.service;

import com.example.demo.config.WeaviateProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeaviateClientService {

    private static final float MAX_DISTANCE = 0.5f;

    private final WeaviateProperties props;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    public record EvidenceChunk(String title, String content, String source) {}

    private HttpRequest.Builder requestBuilder(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(props.getBaseUrl() + path))
                .header("Content-Type", "application/json");

        String apiKey = props.getApiKey();
        if (apiKey != null && !apiKey.isBlank()) {
            builder.header("X-API-KEY", apiKey);
        }

        return builder;
    }

    @PostConstruct
    public void ensureSchema() {
        try {
            log.info("Checking Weaviate schema for ArticleChunk…");
            HttpRequest getSchema = requestBuilder("/v1/schema")
                    .GET()
                    .build();

            HttpResponse<String> schemaResp =
                    client.send(getSchema, HttpResponse.BodyHandlers.ofString());

            JsonNode root = mapper.readTree(schemaResp.body());
            JsonNode classesNode = root.path("classes");

            boolean hasArticleChunk = false;
            if (classesNode.isArray()) {
                for (JsonNode c : classesNode) {
                    if ("ArticleChunk".equalsIgnoreCase(c.path("class").asText())) {
                        hasArticleChunk = true;
                        break;
                    }
                }
            }

            if (!hasArticleChunk) {
                log.info("ArticleChunk class not found. Creating…");
                String body = """
                        {
                          "class": "ArticleChunk",
                          "description": "Small article fragment for fact-checking",
                          "vectorizer": "none",
                          "properties": [
                            { "name": "title",   "dataType": ["text"] },
                            { "name": "content", "dataType": ["text"] },
                            { "name": "source",  "dataType": ["text"] }
                          ]
                        }
                        """;

                HttpRequest createClass = requestBuilder("/v1/schema")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> createResp =
                        client.send(createClass, HttpResponse.BodyHandlers.ofString());
                log.info("Create ArticleChunk schema status={} body={}",
                        createResp.statusCode(), createResp.body());
            } else {
                log.info("ArticleChunk class already exists in Weaviate");
            }
        } catch (Exception e) {
            log.error("ensureSchema() failed", e);
        }
    }

    public String insertArticleChunk(String title,
                                     String content,
                                     String source,
                                     float[] vector) throws Exception {

        StringBuilder vecBuilder = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) vecBuilder.append(",");
            vecBuilder.append(vector[i]);
        }
        vecBuilder.append("]");

        String body = """
                {
                  "class": "ArticleChunk",
                  "properties": {
                    "title": %s,
                    "content": %s,
                    "source": %s
                  },
                  "vector": %s
                }
                """.formatted(
                mapper.writeValueAsString(title),
                mapper.writeValueAsString(content),
                mapper.writeValueAsString(source),
                vecBuilder.toString()
        );

        HttpRequest request = requestBuilder("/v1/objects")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        log.info("insertArticleChunk() status={} body={}", resp.statusCode(), resp.body());
        return resp.body();
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

        log.debug("Weaviate searchByVector response={}", resp.body());
        return resp.body();
    }

    private static String buildQuery(float[] vector, int limit) {
        StringBuilder vecBuilder = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) vecBuilder.append(",");
            vecBuilder.append(vector[i]);
        }
        vecBuilder.append("]");

        return String.format(
                java.util.Locale.US,
                "{ Get { ArticleChunk(nearVector: { vector: %s, distance: %f }, limit: %d) { title content source _additional { distance } } } }",
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

                String title = n.path("title").asText("");
                String content = n.path("content").asText("");
                String source = n.path("source").asText("");

                chunks.add(new EvidenceChunk(title, content, source));
            }
        } else {
            log.warn("No ArticleChunk array in Weaviate response");
        }

        log.info("parseEvidenceChunks() extracted {} chunks", chunks.size());
        return chunks;
    }
}