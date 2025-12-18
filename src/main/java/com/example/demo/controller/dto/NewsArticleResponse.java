package com.example.demo.controller.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class NewsArticleResponse {
    private final Long id;
    private final String title;
    private final String description;
    private final String sourceName;
    private final String externalUrl;
    private final Instant publishedDate;
    private final Instant fetchedAt;
    private final int chunkCount;
    private final String content;
}
