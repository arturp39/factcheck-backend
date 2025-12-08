package com.example.demo.controller;

import com.example.demo.service.VertexEmbeddingService;
import com.example.demo.service.WeaviateClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/articles")
public class ArticleAdminController {

    private final VertexEmbeddingService embeddingService;
    private final WeaviateClientService weaviateClientService;

    @PostMapping("/create")
    public String createArticle(@RequestParam String title,
                                @RequestParam String content,
                                @RequestParam(required = false) String source) throws Exception {

        if (source == null || source.isBlank()) {
            source = "manual";
        }

        float[] vector = embeddingService.embedText(content);
        String weaviateResp = weaviateClientService.insertArticleChunk(title, content, source, vector);

        return "\nWeaviate response:\n" + weaviateResp;
    }
}