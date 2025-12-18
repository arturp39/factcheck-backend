package com.example.demo.controller;

import com.example.demo.controller.dto.NewsArticleResponse;
import com.example.demo.service.NewsArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class NewsArticleController {

    private final NewsArticleService newsArticleService;

    @GetMapping
    public List<NewsArticleResponse> listArticles(
            @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        return newsArticleService.listArticles(limit);
    }

    @GetMapping("/{id}")
    public NewsArticleResponse getArticle(@PathVariable("id") Long id) {
        return newsArticleService.getArticle(id);
    }
}
