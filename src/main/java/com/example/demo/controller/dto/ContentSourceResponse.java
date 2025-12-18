package com.example.demo.controller.dto;

import com.example.demo.entity.content.SourceType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ContentSourceResponse {
    private final Long id;
    private final String name;
    private final SourceType type;
    private final String url;
    private final String category;
    private final boolean enabled;
    private final Double reliabilityScore;
    private final Instant lastFetchedAt;
    private final Instant lastSuccessAt;
    private final int failureCount;
}
