package com.example.demo.service;

import com.example.demo.config.WeaviateProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class WeaviateClientServiceTest {

    @Test
    void parseEvidenceChunks_filtersByDistanceAndMapsFields() throws Exception {
        WeaviateProperties props = new WeaviateProperties();
        props.setBaseUrl("http://localhost:8080");
        props.setApiKey(null);
        props.setMaxDistance(0.5f);

        WeaviateClientService service = new WeaviateClientService(props);

        String json = """
                {
                  "data": {
                    "Get": {
                      "ArticleChunk": [
                        {
                          "text": "Chunk1",
                          "articleTitle": "Title1",
                          "sourceName": "Source1",
                          "_additional": { "distance": 0.3 }
                        },
                        {
                          "text": "TooFar",
                          "articleTitle": "Title2",
                          "sourceName": "Source2",
                          "_additional": { "distance": 0.9 }
                        }
                      ]
                    }
                  }
                }
                """;

        List<WeaviateClientService.EvidenceChunk> chunks =
                service.parseEvidenceChunks(json);

        assertThat(chunks).hasSize(1);
        WeaviateClientService.EvidenceChunk c = chunks.get(0);
        assertThat(c.title()).isEqualTo("Title1");
        assertThat(c.content()).isEqualTo("Chunk1");
        assertThat(c.source()).isEqualTo("Source1");
    }

    @Test
    void parseEvidenceChunks_throwsOnErrorsField() {
        WeaviateProperties props = new WeaviateProperties();
        props.setBaseUrl("http://localhost:8080");
        props.setApiKey(null);
        props.setMaxDistance(0.5f);

        WeaviateClientService service = new WeaviateClientService(props);

        String json = """
                {
                  "errors": [
                    { "message": "Something went wrong" }
                  ]
                }
                """;

        assertThatThrownBy(() -> service.parseEvidenceChunks(json))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Weaviate GraphQL returned errors");
    }
}
