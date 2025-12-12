package com.example.demo.service;

import com.example.demo.entity.Article;
import com.example.demo.entity.ClaimLog;
import com.example.demo.integration.nlp.NlpServiceClient;
import com.example.demo.repository.ClaimLogRepository;
import com.example.demo.service.WeaviateClientService.EvidenceChunk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClaimService {

    private final ClaimLogRepository claimRepo;
    private final NlpServiceClient nlpServiceClient;
    private final WeaviateClientService weaviateClientService;

    public ClaimService(ClaimLogRepository claimRepo,
                        NlpServiceClient nlpServiceClient,
                        WeaviateClientService weaviateClientService) {
        this.claimRepo = claimRepo;
        this.nlpServiceClient = nlpServiceClient;
        this.weaviateClientService = weaviateClientService;
    }

    public List<Article> searchEvidence(String claim) {
        log.info("searchEvidence() called with claim='{}'", claim);

        try {
            String correlationId = UUID.randomUUID().toString();
            log.debug("Generated correlationId={} for claim", correlationId);

            // 1) Embedding via Python NLP service
            float[] claimVector = nlpServiceClient.embedSingleToVector(claim, correlationId);
            log.info("Claim embedding length={}", claimVector.length);

            // 2) Search Weaviate
            String graphqlResponse = weaviateClientService.searchByVector(claimVector, 5);
            List<EvidenceChunk> chunks =
                    weaviateClientService.parseEvidenceChunks(graphqlResponse);

            log.info("Weaviate returned {} evidence chunks", chunks.size());

            // 3) Map to Article DTO for UI
            return chunks.stream()
                    .map(c -> {
                        Article a = new Article();
                        a.setTitle(c.title());
                        a.setContent(c.content());
                        a.setSource(c.source());
                        return a;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Vector search failed for claim='{}'", claim, e);
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
        ClaimLog logEntity = claimRepo.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + claimId));
        logEntity.setModelAnswer(answer);
        claimRepo.save(logEntity);
    }
}