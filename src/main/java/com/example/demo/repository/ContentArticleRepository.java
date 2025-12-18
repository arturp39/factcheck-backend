package com.example.demo.repository;

import com.example.demo.entity.content.ArticleStatus;
import com.example.demo.entity.content.ContentArticle;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContentArticleRepository extends JpaRepository<ContentArticle, Long> {

    @EntityGraph(attributePaths = "source")
    List<ContentArticle> findByStatusAndWeaviateIndexedTrue(ArticleStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "source")
    Optional<ContentArticle> findByIdAndStatusAndWeaviateIndexedTrue(Long id, ArticleStatus status);
}
