package com.example.demo.controller;

import com.example.demo.controller.dto.ClaimLogResponse;
import com.example.demo.entity.ClaimLog;
import com.example.demo.service.ClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/claims")
public class ClaimLogApiController {

    private final ClaimService claimService;

    @GetMapping
    public List<ClaimLogResponse> listClaims(
            @RequestParam(name = "limit", defaultValue = "20") int limit
    ) {
        return claimService.listClaims(limit).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ClaimLogResponse getClaim(@PathVariable("id") Long id) {
        return toResponse(claimService.getClaim(id));
    }

    private ClaimLogResponse toResponse(ClaimLog log) {
        return ClaimLogResponse.builder()
                .id(log.getId())
                .claimText(log.getClaimText())
                .createdAt(log.getCreatedAt())
                .modelAnswer(log.getModelAnswer())
                .verdict(log.getVerdict())
                .explanation(log.getExplanation())
                .biasAnalysis(log.getBiasAnalysis())
                .build();
    }
}
