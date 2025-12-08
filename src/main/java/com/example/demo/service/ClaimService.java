package com.example.demo.service;

import com.example.demo.entity.Article;
import com.example.demo.entity.ClaimLog;
import com.example.demo.repository.ClaimLogRepository;
import com.example.demo.service.WeaviateClientService.EvidenceChunk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClaimService {

    private final ClaimLogRepository claimRepo;
    private final VertexEmbeddingService embeddingService;
    private final WeaviateClientService weaviateClientService;

    public ClaimService(ClaimLogRepository claimRepo,
                        VertexEmbeddingService embeddingService,
                        WeaviateClientService weaviateClientService) {
        this.claimRepo = claimRepo;
        this.embeddingService = embeddingService;
        this.weaviateClientService = weaviateClientService;
    }

    public List<Article> searchEvidence(String claim) {
        log.info("searchEvidence() called with claim='{}'", claim);

        try {
            float[] claimVector = embeddingService.embedText(claim);
            log.info("Claim embedding length={}", claimVector.length);

            String graphqlResponse = weaviateClientService.searchByVector(claimVector, 5);
            List<EvidenceChunk> chunks =
                    weaviateClientService.parseEvidenceChunks(graphqlResponse);

            log.info("Weaviate returned {} evidence chunks", chunks.size());

            List<Article> articles = chunks.stream()
                    .map(c -> {
                        Article a = new Article();
                        a.setTitle(c.title());
                        a.setContent(c.content());
                        a.setSource(c.source());
                        return a;
                    })
                    .collect(Collectors.toList());

            for (Article a : articles) {
                log.debug("Evidence article title='{}' source='{}'",
                        a.getTitle(), a.getSource());
            }

            return articles;
        } catch (Exception e) {
            log.error("Vector search failed", e);
            throw new RuntimeException("Vector search failed: " + e.getMessage(), e);
        }
    }

    public ClaimLog saveClaim(String claim) {
        ClaimLog logEntity = new ClaimLog();
        logEntity.setClaimText(claim);
        ClaimLog saved = claimRepo.save(logEntity);
        log.info("Saved claim id={} text='{}'", saved.getId(), claim);
        return saved;
    }

    public void storeModelAnswer(Long claimId, String answer) {
        log.info("Storing model answer for claimId={}", claimId);
        ClaimLog logEntity = claimRepo.findById(claimId).orElseThrow();
        logEntity.setModelAnswer(answer);
        claimRepo.save(logEntity);
    }
}