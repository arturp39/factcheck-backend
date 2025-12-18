package com.example.demo.service;

import com.example.demo.controller.dto.NewsArticleResponse;
import com.example.demo.entity.content.ArticleStatus;
import com.example.demo.entity.content.ContentArticle;
import com.example.demo.repository.ContentArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsArticleService {

    private static final int MAX_LIMIT = 50;

    private final ContentArticleRepository articleRepository;
    private final WeaviateClientService weaviateClientService;

    @Transactional(readOnly = true)
    public List<NewsArticleResponse> listArticles(int requestedLimit) {
        int limit = Math.max(1, Math.min(requestedLimit, MAX_LIMIT));
        Pageable pageable = PageRequest.of(
                0,
                limit,
                Sort.by(Sort.Direction.DESC, "publishedDate")
                        .and(Sort.by(Sort.Direction.DESC, "id"))
        );

        List<ContentArticle> articles =
                articleRepository.findByStatusAndWeaviateIndexedTrue(ArticleStatus.PROCESSED, pageable);

        List<NewsArticleResponse> responses = new ArrayList<>(articles.size());
        for (ContentArticle article : articles) {
            responses.add(toResponse(article));
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public NewsArticleResponse getArticle(Long id) {
        ContentArticle article = articleRepository
                .findByIdAndStatusAndWeaviateIndexedTrue(id, ArticleStatus.PROCESSED)
                .orElseThrow(() -> new IllegalArgumentException("Article not found or not indexed: " + id));

        return toResponse(article);
    }

    private String fetchFullText(Long articleId) {
        try {
            List<String> chunks = weaviateClientService.getChunksForArticle(articleId);
            if (chunks.isEmpty()) {
                return null;
            }
            return String.join("\n\n", chunks);
        } catch (Exception e) {
            log.warn("Failed to fetch chunks for article {}: {}", articleId, e.getMessage());
            return null;
        }
    }

    private NewsArticleResponse toResponse(ContentArticle article) {
        String content = fetchFullText(article.getId());

        return NewsArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .description(article.getDescription())
                .sourceName(article.getSource() != null ? article.getSource().getName() : null)
                .externalUrl(article.getExternalUrl())
                .publishedDate(article.getPublishedDate())
                .fetchedAt(article.getFetchedAt())
                .chunkCount(article.getChunkCount())
                .content(content)
                .build();
    }
}
