package com.example.demo.controller.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ClaimLogResponse {
    private final Long id;
    private final String claimText;
    private final LocalDateTime createdAt;
    private final String modelAnswer;
    private final String verdict;
    private final String explanation;
    private final String biasAnalysis;
}
