package com.example.demo.service;

import com.example.demo.controller.dto.ContentSourceResponse;
import com.example.demo.entity.content.ContentSource;
import com.example.demo.repository.ContentSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentSourceService {

    private final ContentSourceRepository sourceRepository;

    public List<ContentSourceResponse> listSources(boolean onlyEnabled) {
        List<ContentSource> sources = onlyEnabled
                ? sourceRepository.findAllByEnabledTrueOrderByNameAsc()
                : sourceRepository.findAllByOrderByNameAsc();

        return sources.stream()
                .map(this::toResponse)
                .toList();
    }

    private ContentSourceResponse toResponse(ContentSource source) {
        return ContentSourceResponse.builder()
                .id(source.getId())
                .name(source.getName())
                .type(source.getType())
                .url(source.getUrl())
                .category(source.getCategory())
                .enabled(source.isEnabled())
                .reliabilityScore(source.getReliabilityScore())
                .lastFetchedAt(source.getLastFetchedAt())
                .lastSuccessAt(source.getLastSuccessAt())
                .failureCount(source.getFailureCount())
                .build();
    }
}
