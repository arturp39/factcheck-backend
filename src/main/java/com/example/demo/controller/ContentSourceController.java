package com.example.demo.controller;

import com.example.demo.controller.dto.ContentSourceResponse;
import com.example.demo.service.ContentSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sources")
public class ContentSourceController {

    private final ContentSourceService contentSourceService;

    @GetMapping
    public List<ContentSourceResponse> listSources(
            @RequestParam(name = "enabledOnly", defaultValue = "true") boolean enabledOnly
    ) {
        return contentSourceService.listSources(enabledOnly);
    }
}
