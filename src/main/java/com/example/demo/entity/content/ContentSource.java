package com.example.demo.entity.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "sources", schema = "content")
public class ContentSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SourceType type;

    @Column(nullable = false, columnDefinition = "text")
    private String url;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "reliability_score")
    private Double reliabilityScore;

    @Column(name = "last_fetched_at")
    private Instant lastFetchedAt;

    @Column(name = "last_success_at")
    private Instant lastSuccessAt;

    @Column(name = "failure_count", nullable = false)
    private int failureCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
