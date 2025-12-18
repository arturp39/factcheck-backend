package com.example.demo.entity.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

@Getter
@Setter
@Entity
@Immutable
@Table(name = "articles", schema = "content")
public class ContentArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private ContentSource source;

    @Column(name = "external_url", nullable = false, columnDefinition = "text")
    private String externalUrl;

    @Column(nullable = false, columnDefinition = "text")
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "published_date")
    private Instant publishedDate;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;

    @Column(name = "chunk_count", nullable = false)
    private int chunkCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ArticleStatus status;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "weaviate_indexed", nullable = false)
    private boolean weaviateIndexed;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
